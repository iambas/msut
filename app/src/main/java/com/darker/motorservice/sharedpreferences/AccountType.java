package com.darker.motorservice.sharedpreferences;

import android.content.Context;

/**
 * Created by Darker on 28/11/60.
 */

public class AccountType {
    public static final String CUSTOMER = "user";
    public static final String TRADER = "service";
    public static final String STATUS = "status";

    public static boolean isCustomer(Context context){
        return SharedPreferencesUtil.getLoginPreferences(context).getBoolean(AccountType.CUSTOMER, false);
    }

    public static boolean isTrader(Context context){
        return SharedPreferencesUtil.getLoginPreferences(context).getBoolean(AccountType.TRADER, false);
    }

    public static String getAccountLogin(Context context){
        return SharedPreferencesUtil.getLoginPreferences(context).getString(AccountType.STATUS, "");
    }
}
