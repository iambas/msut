package com.darker.motorservice.firebase;

import android.content.Context;
import android.content.SharedPreferences;

import com.darker.motorservice.sharedpreferences.SharedPreferencesUtil;
import com.darker.motorservice.ui.chat.model.ChatMessageItem;
import com.darker.motorservice.utility.DateUtil;
import com.darker.motorservice.utility.ImageUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static com.darker.motorservice.utility.Constant.DATA;

/**
 * Created by Darker on 26/11/60.
 */

public class FirebaseUtil {

    public class DatabaseChild {
        public static final String ADMIN = "admin";
        public static final String CHAT = "chat";
        public static final String SERVICE = "service";
        public static final String STAT = "stat";
        public static final String STATUS = "status";
        public static final String USER = "user";
        public static final String DATA = "data";
    }

    public static String getUid() {
        return FirebaseAuth.getInstance().getUid();
    }

    public static DatabaseReference getChildData(String child) {
        return FirebaseDatabase.getInstance().getReference().child(child);
    }

    public static StorageReference getChildStorage(String child) {
        return getStorageReference().child(child);
    }

    public static StorageReference getStorageReference() {
        return FirebaseStorage.getInstance().getReference();
    }

    public static void setValueStats(String traderId, final String type) {
        DatabaseReference dbStats = FirebaseUtil.getChildData(StatsConstant.STATS);
        String yearAndMonth = DateUtil.getDateFormat(DateUtil.DatePattern.YEAR_MONTH);
        String date = DateUtil.getDateFormat(DateUtil.DatePattern.DATE);

        final DatabaseReference dbStatsDate = dbStats.child(traderId).child(yearAndMonth).child(date);
        dbStatsDate.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(StatsConstant.CALL)) {
                    dbStatsDate.child(StatsConstant.CALL).setValue(StatsConstant.DEFAULT_VALUE);
                }

                if (!dataSnapshot.hasChild(StatsConstant.CHAT)) {
                    dbStatsDate.child(StatsConstant.CHAT).setValue(StatsConstant.DEFAULT_VALUE);
                }

                String uid = FirebaseUtil.getUid();
                dbStatsDate.child(type).child(uid).setValue(StatsConstant.DEFAULT_VALUE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static void updateMessageToReaded(final Context context) {
        SharedPreferences prefs = SharedPreferencesUtil.getLoginPreferences(context);
        String keyChat = prefs.getString(SharedPreferencesUtil.KEY_CHAT, "");
        if (keyChat.equals(""))
            return;

        DatabaseReference dbChat = FirebaseUtil.getChildData(DatabaseChild.CHAT);
        dbChat = dbChat.child(keyChat).child(DatabaseChild.DATA);
        final DatabaseReference finalDb = dbChat;
        dbChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dsMessage : dataSnapshot.getChildren()) {
                    String chatStatus = dsMessage.child(DatabaseChild.STATUS).getValue().toString();
                    if (!SharedPreferencesUtil.isStatusMessageEqualAccountLogin(context, chatStatus)) {
                        finalDb.child(dsMessage.getKey()).child("read").setValue("อ่านแล้ว");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static void removeChatOlderThanTwoMonth(
            ChatMessageItem chatMessageItem,
            String keyChat,
            String keyMessage) {

        int monthChat = DateUtil.getMonthFromChat(chatMessageItem.getDate());
        if (isOverTwoMonth(monthChat)) {
            removeData(chatMessageItem, keyChat, keyMessage);
        }
    }

    private static boolean isOverTwoMonth(int monthChat) {
        int currentMonth = DateUtil.getCurrentMonth();
        if (monthChat > 10 && monthChat - 10 == currentMonth)
            return true;
        else if (monthChat + 2 <= currentMonth)
            return true;
        return false;
    }

    private static void removeData(
            ChatMessageItem chatMessageItem,
            String keyChat,
            String keyMessage) {


        if (ImageUtil.isImageMessage(chatMessageItem.getMessage())) {
            StorageReference storageRef = FirebaseUtil.getStorageReference();
            String imagePath = ImageUtil.getImagePath(chatMessageItem.getMessage());
            storageRef.child(imagePath).delete();
        }

        DatabaseReference dbChat = FirebaseUtil.getChildData(DatabaseChild.CHAT);
        dbChat.child(keyChat)
                .child(DATA)
                .child(keyMessage)
                .removeValue();
    }
}
