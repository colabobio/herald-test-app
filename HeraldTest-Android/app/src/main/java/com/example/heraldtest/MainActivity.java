package com.example.heraldtest;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {
    /// REQUIRED: Unique permission request code, used by requestPermission and onRequestPermissionsResult.
    private final static int permissionRequestCode = 1249951875;
    private boolean foreground = false;

    private final static String tag = TestApplication.class.getName();

    public final static String USER_CHANNEL_ID = "com.example.heraldtest.user_notifications";
    public final static String SERVICE_CHANNEL_ID = "com.example.heraldtest.service_notifications";

    private final BroadcastReceiver statusChangedReceiver = TestBroadcast.statusChangeReceived(this::updateStatus);
    private final BroadcastReceiver statusDetectedReceiver = TestBroadcast.peerDetectReceived(this::updatePeers);

    Handler handler = new Handler();
    Runnable runnable;

    int delay = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // REQUIRED : Ensure app has all required permissions
        requestPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();

        initNotifications(this);

        final LocalBroadcastManager broadcast = LocalBroadcastManager.getInstance(this);
        broadcast.registerReceiver(statusChangedReceiver, TestBroadcast.statusChangeFilter());
        broadcast.registerReceiver(statusDetectedReceiver, TestBroadcast.peerDetectFilter());

        Context context = getApplicationContext();
        Intent startIntent = new Intent(context, TestService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(startIntent);
        } else {
            context.startService(startIntent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        final LocalBroadcastManager broadcast = LocalBroadcastManager.getInstance(this);
        broadcast.unregisterReceiver(statusChangedReceiver);
        broadcast.unregisterReceiver(statusDetectedReceiver);
    }

    protected void initNotifications(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the notification channels, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            NotificationManager manager = context.getSystemService(NotificationManager.class);

            String name, desc;
            NotificationChannel channel;
            int importance;

            // Create high importance channel for user notifications
            name = "User notifications";
            desc = "User updates are shown in this channel";
            importance = NotificationManager.IMPORTANCE_HIGH;
            channel = new NotificationChannel(USER_CHANNEL_ID, name, importance);
            channel.enableVibration(true);
            channel.setDescription(desc);
            manager.createNotificationChannel(channel);

            // Create minimum importance channel for service notifications
            name = "Service notifications";
            desc = "Updates from running foreground services";
            importance = NotificationManager.IMPORTANCE_MIN;
            channel = new NotificationChannel(SERVICE_CHANNEL_ID, name, importance);
            channel.setDescription(desc);
            manager.createNotificationChannel(channel);
        }
    }

    protected void updateStatus() {
        if (TestService.instance != null) {
            TextView status = findViewById(R.id.status);
            IllnessStatusPayloadDataSupplier supplier = TestService.instance.payloadDataSupplier;
            status.setText(supplier.getIdentifier() + ": " + supplier.getStatus().status);
        }
    }

    protected void updatePeers() {
        EditText peers = findViewById(R.id.peers);
        String txt = "";
        for (Integer id: TestService.instance.currentPeers.keySet()) {
            PeerInfo info = TestService.instance.currentPeers.get(id);
            txt += "->" + id + ":" + info.status.status + ":RSSI=" + info.getRSSI() + "\n";
        }
        peers.setText(txt);
    }


    /// REQUIRED : Request application permissions for sensor operation.
    private void requestPermissions() {
        // Check and request permissions
        final List<String> requiredPermissions = new ArrayList<>();
        requiredPermissions.add(Manifest.permission.BLUETOOTH);
        requiredPermissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        requiredPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            requiredPermissions.add(Manifest.permission.FOREGROUND_SERVICE);
        }
        requiredPermissions.add(Manifest.permission.WAKE_LOCK);
        final String[] requiredPermissionsArray = requiredPermissions.toArray(new String[0]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(requiredPermissionsArray, permissionRequestCode);
        } else {
            ActivityCompat.requestPermissions(this, requiredPermissionsArray, permissionRequestCode);
        }
    }

    /// REQUIRED : Handle permission results.
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == permissionRequestCode) {
            boolean permissionsGranted = true;
            for (int i = 0; i < permissions.length; i++) {
                final String permission = permissions[i];
                if (grantResults[i] != PERMISSION_GRANTED) {
                    Log.e(tag, "Permission denied (permission=" + permission + ")");
                    permissionsGranted = false;
                } else {
                    Log.d(tag, "Permission granted (permission=" + permission + ")");
                }
            }

            if (!permissionsGranted) {
                Log.e(tag, "Application does not have all required permissions to start (permissions=" + Arrays.asList(permissions) + ")");
            }
        }
    }

}