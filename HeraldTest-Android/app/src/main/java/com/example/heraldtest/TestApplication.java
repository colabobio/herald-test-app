package com.example.heraldtest;

import android.app.Application;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

public class TestApplication extends Application {

    public static TestApplication instance;
    private static boolean activityVisible = false;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onAppBackgrounded() {
        activityVisible = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onAppForegrounded() {
        activityVisible = true;
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }
}
