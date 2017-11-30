package com.darker.motorservice.firebase;

import com.darker.motorservice.utility.DateUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by Darker on 26/11/60.
 */

public class FirebaseUtil {

    public static String getUid(){
        return FirebaseAuth.getInstance().getUid();
    }

    public static DatabaseReference getChildData(String child){
        return FirebaseDatabase.getInstance().getReference().child(child);
    }

    public static StorageReference getChildStorage(String child){
        return getStorageReference().child(child);
    }

    public static StorageReference getStorageReference(){
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
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
