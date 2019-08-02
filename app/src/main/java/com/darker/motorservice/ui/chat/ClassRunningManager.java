package com.darker.motorservice.ui.chat;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by Darker on 29/11/60.
 */

public class ClassRunningManager {

    public static boolean isOneActivityRunning(Context context){
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = manager.getRunningTasks(2);

        boolean isTopActivity = taskList.get(0)
                .topActivity
                .getClassName()
                .equals(context
                        .getClass()
                        .getName());

        if (taskList.get(0).numActivities == 1 && isTopActivity) {
            Log.i("Current", "This is last activity in the stack");
            return true;
        }
        return false;
    }
}
