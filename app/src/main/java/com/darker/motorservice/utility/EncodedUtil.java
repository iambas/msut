package com.darker.motorservice.utility;

import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncodedUtil {

    private String result;

    public EncodedUtil(String password) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(password.getBytes());
        result = Base64.encodeToString(md.digest(), Base64.DEFAULT);
        Log.d("encode password", result);
    }

    public String getResult() {
        return result;
    }
}
