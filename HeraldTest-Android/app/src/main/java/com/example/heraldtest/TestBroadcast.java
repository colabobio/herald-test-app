package com.example.heraldtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class TestBroadcast {
    public static final String STATUS_CHANGED = BuildConfig.APPLICATION_ID + ".STATUS_CHANGED";
    public static final String PEER_DETECTED = BuildConfig.APPLICATION_ID + ".PEER_DETECTED";

    @NonNull
    public static BroadcastReceiver statusChangeReceived(@NonNull Runnable callback) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (STATUS_CHANGED.equals(intent.getAction())) {
                    callback.run();
                }
            }
        };
    }

    @NonNull
    public static IntentFilter statusChangeFilter() {
        return new IntentFilter(STATUS_CHANGED);
    }

    public static void triggerStatusChange() {
        LocalBroadcastManager.getInstance(TestApplication.instance).sendBroadcast(
                new Intent(TestBroadcast.STATUS_CHANGED)
        );
    }

    @NonNull
    public static BroadcastReceiver peerDetectReceived(@NonNull Runnable callback) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (PEER_DETECTED.equals(intent.getAction())) {
                    callback.run();
                }
            }
        };
    }

    @NonNull
    public static IntentFilter peerDetectFilter() {
        return new IntentFilter(PEER_DETECTED);
    }

    public static void triggerPeerDetect() {
        LocalBroadcastManager.getInstance(TestApplication.instance).sendBroadcast(
                new Intent(TestBroadcast.PEER_DETECTED)
        );
    }
}
