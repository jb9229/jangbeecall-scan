package com.jangbeecallscan.utils;

import android.app.ActivityManager;
import android.content.Context;

public class ServiceUtils {
    public static Boolean isLaunchingService(Context mContext, Class className){

        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (className.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return  false;
    }
}
