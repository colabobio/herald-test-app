package com.example.flutterxherald;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.flutter.Log;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {

    /// REQUIRED: Unique permission request code, used by requestPermission and
    /// onRequestPermissionsResult.
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

    private String METHOD_CHANNEL_NAME = "com.flutterxherald.methodChannel";

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        Log.i("abc", "2");
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), METHOD_CHANNEL_NAME)
                .setMethodCallHandler((call, result) -> {
                    if (call.method.equals("sendToBackground")) {
                        moveTaskToBack(true);
                        result.success(null);
                    } else if (call.method.equals("isPeersAvailable")) {
                        String Peers = updatePeers();
                        result.success(Peers);
                    } else if (call.method.equals("isStatusAvailable")) {
                        String Status = updateStatus();
                        result.success(Status);
                    } else {
                        result.notImplemented();
                    }

                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    protected String updateStatus() {
        if (TestService.instance != null) {
            IllnessStatusPayloadDataSupplier supplier = TestService.instance.payloadDataSupplier;
            return supplier.getIdentifier() + ": " + supplier.getStatus().status;
        }
        return null;
    }

    protected String updatePeers() {
        if (TestService.instance.updateEditText() != null) {
            return TestService.instance.updateEditText();
        } else {
            return "Not Working!";
        }
    }

    /**
     * REQUIRED : Request application permissions for sensor operation.
     */
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

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
            @NonNull final int[] grantResults) {
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
                Log.e(tag, "Application does not have all required permissions to start (permissions="
                        + Arrays.asList(permissions) + ")");
            }
        }
    }
}
