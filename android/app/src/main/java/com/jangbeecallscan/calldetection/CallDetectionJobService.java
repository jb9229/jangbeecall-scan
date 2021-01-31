package com.jangbeecallscan.calldetection;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CallDetectionJobService extends JobService {
  @Override
  public void onCreate() {
      super.onCreate();
  }

  @Override
  public boolean onStartJob(JobParameters jobParameters) {
      String phoneNumber = jobParameters.getExtras().getString("telNumber");
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
                  String blResult = PhoneNumberUtils.formatNumber(phoneNumber);
//
//                    Intent appStartIntent = new Intent(getApplicationContext(), MainActivity.class);
//                    startActivity(appStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

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

      //getHttpAsync("https", "jangbeecall-dev.azurewebsites.net/graphql", phoneNumber, httpCallBack);
      getHttpAsync("http", "10.0.2.2:4000", phoneNumber, httpCallBack);

      return false;
  }

  @Override
  public boolean onStopJob(JobParameters jobParameters) {
      return false;
  }

  void getHttpAsync(String scheme, String host, String phoneNumber, Callback callback) {
      try {
          OkHttpClient client = new OkHttpClient.Builder()
                  .connectTimeout(30, TimeUnit.SECONDS)
                  .readTimeout(30, TimeUnit.SECONDS)
                  .retryOnConnectionFailure(true)
                  .build();
          HttpUrl.Builder builder = new HttpUrl.Builder();
          builder.scheme(scheme);
          builder.host(host);

//            parameterData.forEach((k, v) -> builder.addQueryParameter(k, v)); // from API24
          JSONObject jsonInput = new JSONObject();
          StringBuilder queryBuilder =  new StringBuilder("query { firmHarmCase(telNumber:\\\"");
          queryBuilder.append(phoneNumber);
          queryBuilder.append("\\\") { accountId cliName } }");

          jsonInput.put("query", queryBuilder.toString());

          Request request = new Request.Builder()
                  .url(builder.build())
                  .addHeader("Content-Type", "application/json")
                  .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonInput.toString()))
                  .build(); // Request

          //동기 처리시 execute 함수 사용
          client.newCall(request).enqueue(callback);
      } catch (Exception e){
          System.err.println(e.toString());
      }
  }
}