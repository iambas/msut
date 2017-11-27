package com.darker.motorservice.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Darker on 26/11/60.
 */

public class FirebaseUtil {

    public static String getUid(){
        return FirebaseAuth.getInstance().getUid();
    }

    public static DatabaseReference getChild(String child){
        return FirebaseDatabase.getInstance().getReference().child(child);
    }
}
