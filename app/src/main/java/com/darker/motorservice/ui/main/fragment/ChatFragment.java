package com.darker.motorservice.ui.main.fragment;

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
import com.darker.motorservice.ui.main.adapter.ChatAdapter;
import com.darker.motorservice.utils.NetWorkUtils;
import com.darker.motorservice.ui.main.model.ChatItem;
import com.darker.motorservice.ui.chat.model.ChatMessageItem;
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

import static com.darker.motorservice.utils.Constant.ALERT;
import static com.darker.motorservice.utils.Constant.CHAT;
import static com.darker.motorservice.utils.Constant.DATA;
import static com.darker.motorservice.utils.Constant.KEY_IMAGE;
import static com.darker.motorservice.utils.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.utils.Constant.NAME;
import static com.darker.motorservice.utils.Constant.PHOTO;
import static com.darker.motorservice.utils.Constant.SERVICE;
import static com.darker.motorservice.utils.Constant.STATUS;
import static com.darker.motorservice.utils.Constant.USER;

public class ChatFragment extends Fragment {

    private Context context;
    private DatabaseReference dbChat, dbChatWith;

    private ChatAdapter chatAdapter;
    private List<ChatItem> chatItemList;
    private ProgressBar progressBar;
    private TextView tvNetworkAlert, tvTextNull;
    private String uid, status, chatChild;
    private SharedPreferences.Editor shedChat;

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
        loginSharedPreference();
        setUpDataFirebase();
        setUpRecyclerView(view);
        bindView(view);
        checkRefreshOrReadData();
    }

    private void bindView(View view) {
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        tvNetworkAlert = (TextView) view.findViewById(R.id.txt_net_alert);
        tvTextNull = (TextView) view.findViewById(R.id.txt_null);
    }

    private void setUpRecyclerView(View view) {
        chatItemList = new ArrayList<ChatItem>();
        chatAdapter = new ChatAdapter(context, chatItemList);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(chatAdapter);
    }

    private void setUpDataFirebase() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();
        chatChild = status.equals(USER) ? SERVICE : USER;
        dbChat = FirebaseDatabase.getInstance().getReference().child(CHAT);
        dbChatWith = FirebaseDatabase.getInstance().getReference().child(chatChild);
    }

    private void loginSharedPreference() {
        SharedPreferences shLogin = context.getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        status = shLogin.getString(STATUS, "");
        shedChat = context.getSharedPreferences(CHAT, Context.MODE_PRIVATE).edit();
        shedChat.putBoolean(ALERT, false);
        shedChat.apply();
    }

    private void checkRefreshOrReadData() {
        progressBar.setVisibility(View.VISIBLE);
        if (NetWorkUtils.disable(context)) {
            tvNetworkAlert.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            tvNetworkAlert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkRefreshOrReadData();
                }
            });
        } else {
            tvNetworkAlert.setVisibility(View.GONE);
            readData();
        }
    }

    public void readData() {
        dbChat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataChat) {
                chatItemList.clear();
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
                    tvTextNull.setVisibility(View.VISIBLE);
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
                        ChatMessageItem chat = new ChatMessageItem();
                        for (DataSnapshot ds : dataLast.getChildren()) {
                            chat = ds.getValue(ChatMessageItem.class);
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

                        final ChatItem myChatItem = new ChatItem(keyChat, chatWithId, chatWithName, msg, chat.getDate(), chat.getRead(), chat.getStatus(), photo);
                        chatItemList.add(myChatItem);
                        Collections.sort(chatItemList, new Comparator<ChatItem>() {
                            @Override
                            public int compare(ChatItem s1, ChatItem s2) {
                                return s2.toString().compareToIgnoreCase(s1.toString());
                            }
                        });
                        chatAdapter.notifyDataSetChanged();
                        list();
                        tvTextNull.setVisibility(View.GONE);
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
        int sr = chatItemList.size() - 1;
        for (int i = 0; i < sr; i++) {
            String s1 = chatItemList.get(i).getKeyChat();
            String s2 = chatItemList.get(i + 1).getKeyChat();
            if (s1.equals(s2)) {
                chatItemList.remove(chatItemList.get(i));
                i--;
                sr--;
            }
            chatAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        shedChat.putBoolean(ALERT, false);
        shedChat.commit();
    }
}
