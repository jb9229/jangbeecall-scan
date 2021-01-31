package com.jangbeecallscan.calldetection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.jangbeecallscan.utils.ReactAsyncStorageUtils;


public class BootReceiver extends BroadcastReceiver {
  private static String mLastState;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BootTest : ", "\nOnBootReceiver - Received a broadcast!");
        Toast.makeText(context, "OnBootReceiver Received a broadcast!!", Toast.LENGTH_LONG).show();

        boolean isScanBlackList = ReactAsyncStorageUtils.retrieveBoolean(context, ReactAsyncStorageUtils.ISSCANBALCKLIST_SPKEY);
        if(!isScanBlackList) {return;}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, PhoneStateService.class));
        } else {
            context.startService(new Intent(context, PhoneStateService.class));
        }
    }
}
