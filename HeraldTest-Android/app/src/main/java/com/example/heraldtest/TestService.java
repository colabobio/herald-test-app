package com.example.heraldtest;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.Random;

import androidx.core.app.NotificationCompat;
import io.heraldprox.herald.sensor.datatype.Date;

public class TestService extends Service {
    private static final String tag = "TestService";

    private static final int FOREGROUND_NOTIFICATION_ID = 133;
    private static final int TIME_STEP = 2;

    public static TestService instance;

    public String id;
    public String state;

    private int time0;

    private Handler handler = new Handler();
    private Runnable runnable;

    public TestService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        goToForeground();
        initState();
        updateLoop();
    }

    private void goToForeground() {
        Notification notification = new NotificationCompat.Builder(this, MainActivity.SERVICE_CHANNEL_ID)
                .setContentTitle("Listening to peers")
                .setContentText("Background process to listen to peers")
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

    private void initState() {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        id = generateRandomString(letters, 2) + generateRandomString(digits, 2);
        time0 = (int)(System.currentTimeMillis() / 1000);
        state = id + ":0";
    }

    private String generateRandomString(String chars, int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    private void updateState() {
        state = id + ":" + (System.currentTimeMillis() / 1000 - time0);

        updatePayload();

        TestBroadcast.triggerStatusChange();
    }

    private void updatePayload() {
        MainActivity.payloadDataSupplier.setStatus(new IllnessStatus(IllnessStatusCode.getRandomStatus() ,new Date()));
//        TestApplication.payloadDataSupplier.payload(new Data(state));
    }

    private void updateLoop() {
        updateState();

        Log.i(tag, "in update loop");

        runnable = () -> handler.post(TestService.this::updateLoop);
        handler.postDelayed(runnable, TIME_STEP * 1000);
    }
}