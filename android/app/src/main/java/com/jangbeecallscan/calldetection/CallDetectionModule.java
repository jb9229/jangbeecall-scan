package com.jangbeecallscan.calldetection;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.IllegalViewOperationException;
import com.jangbeecallscan.utils.ReactAsyncStorageUtils;
import com.jangbeecallscan.utils.ServiceUtils;

import javax.annotation.Nonnull;

public class CallDetectionModule extends ReactContextBaseJavaModule {
  // Variables
  static int PHONE_STATE_REQCODE = 3;
  static int REQ_CODE_OVERLAY_PERMISSION = 2;

  private static ReactApplicationContext reactContext;
  public CallDetectionModule(ReactApplicationContext reactContext) {
      super(reactContext);
      this.reactContext = reactContext;
  }

  @Nonnull
  @Override
  public String getName() {
      return "CallDetection";
  }

  public static void sendEvent(String event, WritableNativeMap params) {
      reactContext
              .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
              .emit(event, params);
  }

  @ReactMethod
  public void isRunningService( Callback successCallback, Callback errorCallback) {
      try {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(reactContext)) {
              Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + reactContext.getPackageName()));
              reactContext.startActivityForResult(intent, REQ_CODE_OVERLAY_PERMISSION, new Bundle());
          }

          if(checkPhonestatePermission()) {
              // Check BlackList Scan Setting Value
              boolean isScanBlackList = ReactAsyncStorageUtils.retrieveBoolean(reactContext, ReactAsyncStorageUtils.ISSCANBALCKLIST_SPKEY);

              boolean isRunningService = ServiceUtils.isLaunchingService(reactContext, PhoneStateService.class);
              if(isRunningService && isScanBlackList) {
                  successCallback.invoke(isScanBlackList);
              } else {
                  successCallback.invoke(false);
              }
          } else {
              errorCallback.invoke("권한 설정을 완료해 주세요");
          }
      } catch (IllegalViewOperationException e) {
          errorCallback.invoke(e.getMessage());
      }
  }

  @ReactMethod
  public void start(Callback successCallback, Callback errorCallback) {
      Log.d("ReactMethod", "ReactMethod start~~~");
      try{
          if(checkPhonestatePermission()) {
              Intent serviceIntent = new Intent(reactContext, PhoneStateService.class);
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                  Log.d("ReactMethod", "checkPhonestatePermission Build.VERSION.SDK_INT ~~~");
                  reactContext.startForegroundService(serviceIntent);
              } else {
                  Log.d("ReactMethod", "checkPhonestatePermission Build.VERSION_CODES.O ~~~");
                  reactContext.startService(serviceIntent);
              }

              ReactAsyncStorageUtils.storeBoolean(reactContext, ReactAsyncStorageUtils.ISSCANBALCKLIST_SPKEY, true);

              boolean isRunningService = ServiceUtils.isLaunchingService(reactContext, PhoneStateService.class);

              boolean isScanBlackList = ReactAsyncStorageUtils.retrieveBoolean(reactContext, ReactAsyncStorageUtils.ISSCANBALCKLIST_SPKEY);

              if(isRunningService && isScanBlackList) {
                  successCallback.invoke(true);
              } else {
                  successCallback.invoke(false);
              }
          } else {
              successCallback.invoke(false);
          }
      } catch (IllegalViewOperationException e) {
          errorCallback.invoke(e.getMessage());
      }
  }

  @ReactMethod
  public void finish(Callback successCallback, Callback errorCallback) {
      try{
          Intent serviceIntent = new Intent(reactContext, PhoneStateService.class);

          reactContext.stopService(serviceIntent);


          ReactAsyncStorageUtils.storeBoolean(reactContext, ReactAsyncStorageUtils.ISSCANBALCKLIST_SPKEY, false);

          boolean isRunningService = ServiceUtils.isLaunchingService(reactContext, PhoneStateService.class);

          boolean isScanBlackList = ReactAsyncStorageUtils.retrieveBoolean(reactContext, ReactAsyncStorageUtils.ISSCANBALCKLIST_SPKEY);
          if(!isRunningService && !isScanBlackList) {
              successCallback.invoke(false);
          } else {
              successCallback.invoke(isScanBlackList);
          }
      } catch (IllegalViewOperationException e) {
          errorCallback.invoke(e.getMessage());
      }
  }

  boolean checkPhonestatePermission() {
      boolean checkResult = true;

      Activity currentActivity = getCurrentActivity();
      if ( ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
              || ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
          ActivityCompat.requestPermissions(currentActivity, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG}, PHONE_STATE_REQCODE);
          checkResult = false;
      }
//        if ( ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(currentActivity, new String[]{Manifest.permission.READ_PHONE_STATE}, PHONE_STATE_REQCODE);
//            checkResult = false;
//        }

      return checkResult;
  }

  /**
   * 위험한 권한(READ_PHONE_STATE) 요청 결과 호출함수
   *
   * @param requestCode
   * @param permissions
   * @param grantResults
   */
  protected void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
      switch (requestCode) {
          case 3: {  // PHONE_STATE_REQCODE = 3 요청시 임의 상수값
              if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                  // permission granted!
                  // you may now do the action that requires this permission

              } else {
                  // permission denied
                  // TODO send message to js
              }
              return;
          }

      }
  }
}
