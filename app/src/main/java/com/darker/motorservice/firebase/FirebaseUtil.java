package com.darker.motorservice.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
}
