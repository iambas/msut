package com.darker.motorservice.utility;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Darker on 28/11/60.
 */

public class ToasAlert {

    public static void alert(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void alert(Context context, int resource){
        String message = context.getString(resource);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
