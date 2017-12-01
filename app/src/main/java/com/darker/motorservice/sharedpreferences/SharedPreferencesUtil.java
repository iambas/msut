package com.darker.motorservice.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.darker.motorservice.utility.Constant;

/**
 * Created by Darker on 28/11/60.
 */

public class SharedPreferencesUtil {
    private static final String KEY_LOGIN_MOTOR_SERVICE = "login_motor_service";
    private static final String KEY_CHAT_ALERT = "chat";
    public static final String KEY_CHAT = "key_chat";
    private static final String ALERT = "alert";

    public static SharedPreferences getLoginPreferences(Context context){
        return context.getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getChatAlertPreferences(Context context){
        return context.getSharedPreferences(KEY_CHAT_ALERT, Context.MODE_PRIVATE);
    }

    public static void enableChatAlert(Context context){
        SharedPreferences.Editor editor = getChatAlertPreferences(context).edit();
        editor.putBoolean(ALERT, true);
        editor.apply();
    }

    public static void disableChatAlert(Context context){
        SharedPreferences.Editor editor = getChatAlertPreferences(context).edit();
        editor.putBoolean(ALERT, false);
        editor.apply();
    }

    public static boolean isChatAlert(Context context){
        return getChatAlertPreferences(context).getBoolean(ALERT, false);
    }

    public static boolean isStatusMessageEqualAccountLogin(Context context, String status){
        SharedPreferences prefs = getLoginPreferences(context);
        String accountLogin = prefs.getString(Constant.STATUS, "");
        return status.equals(accountLogin);
    }
}
