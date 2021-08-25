package com.example.heraldtest;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.UUID;

public class TestService extends Service {
    private static final String TAG = "TestService";
    public static final String SERVICE_CHANNEL_ID = "com.example.heraldtest.service_notifications";
    private static final int FOREGROUND_NOTIFICATION_ID = 133;
    private static final int TIME_STEP = 2;

    public static TestService instance;

    public String id;
    public String state;

    private Handler handler = new Handler(); // @Edison: Handler is marked as deprecated, what's the replacement for this?
    private Runnable runnable;

    public TestService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        goToForeground();
        updateLoop();
    }

    private void goToForeground() {
        Notification notification = new NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
                .setOngoing(true)
                .build();

        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
    }

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // run until explicitly stopped.
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new TestServiceBinder(this);
    }

    private void updateState() {
        id = UUID.randomUUID().toString();
        state = id + ":" + System.currentTimeMillis() / 1000;

        updatePayload();

        TestBroadcast.triggerStatusChange();
    }

    private void updatePayload() {
        // @Edison: need to update Herald payload here.
    }

    private void updateLoop() {
        updateState();

        runnable = () -> handler.post(TestService.this::updateLoop);
        handler.postDelayed(runnable, TIME_STEP * 1000);
    }
}