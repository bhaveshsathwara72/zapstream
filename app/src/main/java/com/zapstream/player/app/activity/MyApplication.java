package com.zapstream.player.app.activity;

import android.content.Context;
import android.os.StrictMode;

import androidx.multidex.MultiDex;
import androidx.nemosofts.Application;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.onesignal.OneSignal;

import com.zapstream.player.app.BuildConfig;
import com.zapstream.player.app.R;
import com.zapstream.player.app.utils.ApplicationUtil;
import com.zapstream.player.app.utils.helper.DBHelper;
import com.zapstream.player.app.utils.helper.Helper;

public class MyApplication extends Application {

    DBHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        // Analytics Initialization
        FirebaseAnalytics.getInstance(getApplicationContext());

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        try {
            dbHelper = new DBHelper(getApplicationContext());
            dbHelper.onCreate(dbHelper.getWritableDatabase());
        } catch (Exception e) {
            ApplicationUtil.log("MyApplication", "Error DB", e);
        }

        // OneSignal Initialization
        OneSignal.initWithContext(this, getString(R.string.onesignal_app_id));

        new Helper(getApplicationContext()).initializeAds();
    }

    @Override
    public String setProductID() {
        return "52621164";
    }

    @Override
    public String setApplicationID() {
        return BuildConfig.APPLICATION_ID;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
