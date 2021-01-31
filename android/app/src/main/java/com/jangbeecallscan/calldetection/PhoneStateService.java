package com.jangbeecallscan.calldetection;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PersistableBundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.jangbeecallscan.MainActivity;
import com.jangbeecallscan.R;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PhoneStateService extends Service {
    private static final int JOB_ID_UPDATE = 0x1000;
    NotificationManager blNotifyManager;
    NotificationCompat.Builder blBuilder;
    NotificationChannel notificationChannel;
    String NOTIFIVATION_CHANNEL_ID = "17";
    String incomingNumber = "";


    Callback httpCallBack = new Callback() {
        //비동기 처리를 위해 Callback 구현
        @Override
        public void onFailure(Call call, IOException e) {
            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "[장비 콜]수신전화번호 Error: "+e.toString(), Toast.LENGTH_LONG).show();
                };
            }, 0);
            Log.d("JB Server Error: ",  e.toString());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String result   = response.body().string();
            if (result != null && !result.isEmpty() && Boolean.parseBoolean(result)) {
                String blResult = PhoneNumberUtils.formatNumber(incomingNumber);

                Intent serviceIntent = new Intent(getApplicationContext(), IncomingCallBLPopupService.class);
                serviceIntent.putExtra(IncomingCallBLPopupService.INCOMINGCALL_NUMBER_EXTRA, blResult);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
            }
        }
    };

    BroadcastReceiver blBroadcastReceiver = new BroadcastReceiver() {
        String preState;
        String prePhoneNumber;
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("RECEIVER : ", "IS UP Phone State...");
            try
            {
                // Check Duplicate Receive
                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                if (state.equals(preState) && phoneNumber == null) {
                    return;
                } else {
                    preState = state;
                    prePhoneNumber = phoneNumber;
                }

                if(TelephonyManager.EXTRA_STATE_RINGING.equals(state) && phoneNumber != null){
//                    Toast.makeText(context, "[장비 콜]수신전화번호: "+incomingNumber, Toast.LENGTH_LONG).show();

                    Map paramData = new HashMap(); //
//                    phoneNumber = "01052023337";
                    if(phoneNumber == null || phoneNumber.trim().isEmpty()) {Toast.makeText(context, "["+phoneNumber+"]"+"유효하지 않은 번호입니다.", Toast.LENGTH_LONG).show(); return ;} else {paramData.put("telNumber", phoneNumber);}


                    PersistableBundle bundle = new PersistableBundle();
                    bundle.putString("telNumber", phoneNumber);

                    JobInfo jobInfo = new JobInfo.Builder(JOB_ID_UPDATE, new ComponentName(getApplicationContext(), CallDetectionJobService.class))
                            .setExtras(bundle)
                            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                            .setMinimumLatency(1)
                            .setOverrideDeadline(1)
                            .build();

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        JobScheduler jobScheduler = (JobScheduler) getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
                        // Job을 등록한다.
                        jobScheduler.schedule(jobInfo);
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
                Log.e("Receiver : ",  "Exception  is : ", e);
            }
        }
    };

    public PhoneStateService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("PhoneStateService", "PhoneStateService onCreate~~~");
        IntentFilter callFilter = new IntentFilter();
        callFilter.addAction("android.intent.action.PHONE_STATE");
        this.registerReceiver(blBroadcastReceiver, callFilter);

        Intent startAppIntent = new Intent(this, MainActivity.class);
        PendingIntent startAppPendingIntent = PendingIntent.getActivity(this, 1, startAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        blNotifyManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        blBuilder = new NotificationCompat.Builder(this, null);
        blBuilder.setContentTitle("수신 전화 피해사례 확인 중")
                .setContentText("수신전화번호를 확인하여 피해사례가 있는지 알려 드립니다")
                .setContentIntent(startAppPendingIntent)
                .setTicker("Checking New Numbers")
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setDefaults(Notification.DEFAULT_ALL)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(NOTIFIVATION_CHANNEL_ID, "Black List Notifications", NotificationManager.IMPORTANCE_HIGH); // IMPORTANCE_NONE

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            blNotifyManager.createNotificationChannel(notificationChannel);

            blBuilder.setChannelId(NOTIFIVATION_CHANNEL_ID);
            startForeground(17, blBuilder.build());
        } else {
            blBuilder.setChannelId(NOTIFIVATION_CHANNEL_ID);
            //startForeground(17, blBuilder.build());
            blNotifyManager.notify(17, blBuilder.build());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int staertId) {
        Log.d("PhoneStateService : ", "\nblBroadcastReceiver Listening....");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        blNotifyManager.cancel(17);
        if (blBroadcastReceiver != null) {
            this.unregisterReceiver(blBroadcastReceiver);
        }
        Log.d("PhoneStateService : ", "\nDestoryed....");
        Log.d("PhoneStateService : ", "\nWill be created again....");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void searchFirmHarmCase(Map paramData) {
        String[] pathParamArr = {"api", "v1", "client", "evaluation", "exist", "telnumber"};
        getHttpAsync1("http", "jangbeecall.ap-northeast-2.elasticbeanstalk.com", pathParamArr, paramData, httpCallBack);
    }
    void getHttpAsync1(String scheme, String host, String[] pathSegment, Map<String, String> parameterData, Callback callback) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();
            HttpUrl.Builder builder = new HttpUrl.Builder();
            builder.scheme(scheme);
            builder.host(host);

            for (String param : pathSegment) {
                builder.addPathSegment(param);
            }

//            parameterData.forEach((k, v) -> builder.addQueryParameter(k, v)); // from API24
            for (String key : parameterData.keySet()) {
                builder.addQueryParameter(key, parameterData.get(key));
            }

            Request request = new Request.Builder()
                    .url(builder.build())
                    .build(); //GET Request

            //동기 처리시 execute함수 사용
            client.newCall(request).enqueue(callback);
        } catch (Exception e){
            System.err.println(e.toString());
        }
    }

}

