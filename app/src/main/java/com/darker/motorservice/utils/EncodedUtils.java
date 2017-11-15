package com.darker.motorservice.utils;

import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncodedUtils {

    private String result;

    public EncodedUtils(String password) {
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
