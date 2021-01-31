package com.jangbeecallscan.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.modules.storage.ReactDatabaseSupplier;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class ReactAsyncStorageUtils {
    // Variables
    public static String STORFILE_ISSCAN_BLACKLIST = "STOREFILE_ISSCAN_BLACKLIST" ;
    public static String ISSCANBALCKLIST_SPKEY = "ISSCAN_BALCKLIST";


    public static boolean retrieveBoolean (Context context, String key) {
        //SharedPreferences를 sFile이름, 기본모드로 설정
        SharedPreferences sharedPreferences = context.getSharedPreferences(STORFILE_ISSCAN_BLACKLIST,context.MODE_PRIVATE);

        return sharedPreferences.getBoolean(key,false);
    }

    public static void storeBoolean (Context context, String key, Boolean value) {
        //SharedPreferences를 sFile이름, 기본모드로 설정
        SharedPreferences sharedPreferences = context.getSharedPreferences(STORFILE_ISSCAN_BLACKLIST,context.MODE_PRIVATE);

        //저장을 하기위해 editor를 이용하여 값을 저장시켜준다.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value); // key, value를 이용하여 저장하는 형태
        editor.commit();
    }
}
