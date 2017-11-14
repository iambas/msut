package com.darker.motorservice.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.darker.motorservice.R;
import com.darker.motorservice.adapter.ChatAdapter;
import com.darker.motorservice.utils.NetWorkUtils;
import com.darker.motorservice.model.Chat;
import com.darker.motorservice.model.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.darker.motorservice.Constant.ALERT;
import static com.darker.motorservice.Constant.CHAT;
import static com.darker.motorservice.Constant.DATA;
import static com.darker.motorservice.Constant.KEY_IMAGE;
import static com.darker.motorservice.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.Constant.NAME;
import static com.darker.motorservice.Constant.PHOTO;
import static com.darker.motorservice.Constant.SERVICE;
import static com.darker.motorservice.Constant.STATUS;
import static com.darker.motorservice.Constant.USER;

public class ChatFragment extends Fragment {

    private Context context;

    private DatabaseReference dbChat, dbChatWith;

    private ChatAdapter adapter;
    private List<Chat> chatList;
    private ProgressBar progressBar;
    private TextView netAlert, txtNull;
    private String uid, status, chatChild;
    private SharedPreferences.Editor edChat;

    public ChatFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = view.getContext();
        SharedPreferences shLogin = context.getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        status = shLogin.getString(STATUS, "");
        edChat = context.getSharedPreferences(CHAT, Context.MODE_PRIVATE).edit();
        edChat.putBoolean(ALERT, false);
        edChat.commit();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();
        chatChild = status.equals(USER) ? SERVICE : USER;
        dbChat = FirebaseDatabase.getInstance().getReference().child(CHAT);
        dbChatWith = FirebaseDatabase.getInstance().getReference().child(chatChild);

        chatList = new ArrayList<Chat>();
        adapter = new ChatAdapter(context, chatList);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        netAlert = (TextView) view.findViewById(R.id.txt_net_alert);
        txtNull = (TextView) view.findViewById(R.id.txt_null);
        refresh();
    }

    private void refresh() {
        progressBar.setVisibility(View.VISIBLE);
        if (NetWorkUtils.disable(context)) {
            netAlert.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            netAlert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refresh();
                }
            });
        } else {
            netAlert.setVisibility(View.GONE);
            readData();
        }
    }

    public void readData() {
        dbChat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataChat) {
                chatList.clear();
                boolean has = false;
                for (final DataSnapshot data : dataChat.getChildren()) {
                    if (!data.hasChild(DATA)) continue;
                    if (data.child(status).getValue().toString().equals(uid)) {
                        has = true;
                        final String keyChat = data.getKey();
                        final String chatWithId = data.child(chatChild).getValue().toString();

                        dbChatWith.child(chatWithId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataChatWith) {
                                try {
                                    String chatWithName = dataChatWith.child(NAME).getValue().toString();
                                    String photo = dataChatWith.child(PHOTO).getValue().toString();
                                    addList(data, keyChat, chatWithId, chatWithName, photo);
                                } catch (NullPointerException e) {
                                    Log.d("Exception ChatFrag name", e.getMessage());
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                }

                if (!has) {
                    progressBar.setVisibility(View.GONE);
                    txtNull.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void addList(DataSnapshot data, final String keyChat, final String chatWithId, final String chatWithName, final String photo) {
        dbChat.child(data.getKey()).child(DATA).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataData) {
                final Query query = dbChat.child(keyChat).child(DATA).limitToLast(1);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataLast) {
                        ChatMessage chat = new ChatMessage();
                        for (DataSnapshot ds : dataLast.getChildren()) {
                            chat = ds.getValue(ChatMessage.class);
                        }

                        String msg = "";
                        try {
                            msg = chat.getMessage();
                        } catch (NullPointerException e) {
                            Log.d("Exception ChatFrag msg", e.getMessage());
                            return;
                        }
                        final String[] arrMsg = msg.split(": ");
                        if (arrMsg[0].equals("lat/lng")) {
                            if (status.equals(chat.getStatus())) {
                                msg = "คุณได้ส่งตำแหน่ง GPS";
                            } else {
                                msg = chat.getSender() + " ได้ส่งตำแหน่ง GPS";
                            }
                        } else if (chat.getMessage().contains(KEY_IMAGE)) {
                            msg = status.equals(chat.getStatus()) ? "คุณได้ส่งรูปภาพ" : chat.getSender() + " ได้ส่งรูปภาพ";
                        } else {
                            msg = status.equals(chat.getStatus()) ? "คุณ: " + msg : msg;
                        }

                        final Chat myChat = new Chat(keyChat, chatWithId, chatWithName, msg, chat.getDate(), chat.getRead(), chat.getStatus(), photo);
                        chatList.add(myChat);
                        Collections.sort(chatList, new Comparator<Chat>() {
                            @Override
                            public int compare(Chat s1, Chat s2) {
                                return s2.toString().compareToIgnoreCase(s1.toString());
                            }
                        });
                        adapter.notifyDataSetChanged();
                        list();
                        txtNull.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void list() {
        Log.d("list", "OK");
        int sr = chatList.size() - 1;
        for (int i = 0; i < sr; i++) {
            String s1 = chatList.get(i).getKeyChat();
            String s2 = chatList.get(i + 1).getKeyChat();
            if (s1.equals(s2)) {
                chatList.remove(chatList.get(i));
                i--;
                sr--;
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        edChat.putBoolean(ALERT, false);
        edChat.commit();
    }
}
