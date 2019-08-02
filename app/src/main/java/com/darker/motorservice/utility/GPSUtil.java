package com.darker.motorservice.utility;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

/**
 * Created by Darker on 28/11/60.
 */

public class GPSUtil {

    public static final String KEY_LAT_LNG = "lat/lng";
    public static boolean isGPSEnable(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void gpsSettings(Context context){
        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    public static boolean isGPSMessage(String message){
        String[] arr = message.split(": ");
        return arr[0].equals(KEY_LAT_LNG);
    }

    public static String getLatLngFromMessage(String message){
        return message.split(": ")[1];
    }
}
