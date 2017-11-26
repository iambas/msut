package com.darker.motorservice.firebase;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Darker on 26/11/60.
 */

public class FirebaseUtil {

    public static String getUid(){
        return FirebaseAuth.getInstance().getUid();
    }
}
