package com.darker.motorservice.ui.main.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.darker.motorservice.utility.NetworkUtil;
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

import static com.darker.motorservice.utility.Constant.ALERT;
import static com.darker.motorservice.utility.Constant.CHAT;
import static com.darker.motorservice.utility.Constant.DATA;
import static com.darker.motorservice.utility.ImageUtil.KEY_IMAGE;
import static com.darker.motorservice.utility.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.utility.Constant.NAME;
import static com.darker.motorservice.utility.Constant.PHOTO;
import static com.darker.motorservice.utility.Constant.SERVICE;
import static com.darker.motorservice.utility.Constant.STATUS;
import static com.darker.motorservice.utility.Constant.USER;

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
        if (!NetworkUtil.isNetworkAvailable(context)) {
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
                for (final DataSnapshot dsChild : dataChat.getChildren()) {
                    if (!dsChild.hasChild(DATA)) continue;
                    String idOfStatus = dsChild.child(status).getValue().toString();
                    if (idOfStatus.equals(uid)) {
                        has = true;
                        final String keyChat = dsChild.getKey();
                        final String chatWithId = dsChild.child(chatChild).getValue().toString();
                        queryOfChatWithId(dsChild, keyChat, chatWithId);
                    }
                }
                checkHasData(has);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void checkHasData(boolean has) {
        if (!has) {
            progressBar.setVisibility(View.GONE);
            tvTextNull.setVisibility(View.VISIBLE);
        }
    }

    private void queryOfChatWithId(final DataSnapshot dsChild, final String keyChat, final String chatWithId) {
        dbChatWith.child(chatWithId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataChatWith) {
                try {
                    String chatWithName = dataChatWith.child(NAME).getValue().toString();
                    String photo = dataChatWith.child(PHOTO).getValue().toString();
                    queryChatData(dsChild, keyChat, chatWithId, chatWithName, photo);
                } catch (NullPointerException e) {
                    Log.d("Exception ChatFrag name", e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void queryChatData(DataSnapshot data, final String keyChat, final String chatWithId, final String chatWithName, final String photo) {
        dbChat.child(data.getKey()).child(DATA).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataData) {
                queryLastDataChat(keyChat, chatWithId, chatWithName, photo);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void queryLastDataChat(final String keyChat, final String chatWithId, final String chatWithName, final String photo) {
        final Query query = dbChat.child(keyChat).child(DATA).limitToLast(1);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataLast) {
                ChatMessageItem chatMessageItem = new ChatMessageItem();
                for (DataSnapshot ds : dataLast.getChildren()) {
                    chatMessageItem = ds.getValue(ChatMessageItem.class);
                }

                String msg = "";
                try {
                    msg = chatMessageItem.getMessage();
                } catch (NullPointerException e) {
                    Log.d("Exception ChatFrag msg", e.getMessage());
                    return;
                }

                msg = getModifyMessage(chatMessageItem, msg);
                setChatItemToList(chatMessageItem, msg, keyChat, chatWithId, chatWithName, photo);
                sortChatList();
                removeDuplicate();
                unVisbleView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setChatItemToList(ChatMessageItem chatMessageItem, String msg, String keyChat, String chatWithId, String chatWithName, String photo) {
        ChatItem chatItem = new ChatItem();
        chatItem.setKeyChat(keyChat);
        chatItem.setChatWithId(chatWithId);
        chatItem.setChatWithName(chatWithName);
        chatItem.setMessage(msg);
        chatItem.setDate(chatMessageItem.getDate());
        chatItem.setRead(chatMessageItem.getRead());
        chatItem.setStatus(chatMessageItem.getStatus());
        chatItem.setPhoto(photo);
        chatItemList.add(chatItem);
    }

    @NonNull
    private String getModifyMessage(ChatMessageItem chatItem, String msg) {
        final String[] arrMsg = msg.split(": ");
        if (arrMsg[0].equals("lat/lng")) {
            if (status.equals(chatItem.getStatus())) {
                msg = "คุณได้ส่งตำแหน่ง GPS";
            } else {
                msg = chatItem.getSender() + " ได้ส่งตำแหน่ง GPS";
            }
        } else if (chatItem.getMessage().contains(KEY_IMAGE)) {
            msg = status.equals(chatItem.getStatus()) ? "คุณได้ส่งรูปภาพ" : chatItem.getSender() + " ได้ส่งรูปภาพ";
        } else {
            msg = status.equals(chatItem.getStatus()) ? "คุณ: " + msg : msg;
        }
        return msg;
    }

    private void sortChatList() {
        Collections.sort(chatItemList, new Comparator<ChatItem>() {
            @Override
            public int compare(ChatItem s1, ChatItem s2) {
                return s2.toString().compareToIgnoreCase(s1.toString());
            }
        });
        chatAdapter.notifyDataSetChanged();
    }

    private void unVisbleView() {
        tvTextNull.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void removeDuplicate() {
        Log.d("list", "OK");
        int sizeLoop = chatItemList.size() - 1;
        for (int i = 0; i < sizeLoop; i++) {
            String keyChat1 = chatItemList.get(i).getKeyChat();
            String keyChat2 = chatItemList.get(i + 1).getKeyChat();
            if (keyChat1.equals(keyChat2)) {
                chatItemList.remove(chatItemList.get(i));
                i--;
                sizeLoop--;
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
