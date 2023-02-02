package com.example.herald_flutter;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.flutter.Log;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {

    private final static String tag = TestApplication.class.getName();

    /// REQUIRED: Unique permission request code, used by requestPermission and
    /// onRequestPermissionsResult.
    private final static int permissionRequestCode = 1249951875;

    static final String BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT";
    static final String BLUETOOTH_ADVERTISE = "android.permission.BLUETOOTH_ADVERTISE";
    static final String BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN";

    public final static String USER_CHANNEL_ID = "com.example.heraldtest.user_notifications";
    public final static String SERVICE_CHANNEL_ID = "com.example.heraldtest.service_notifications";
    private String METHOD_CHANNEL_NAME = "com.herald_flutter.methodChannel";
    private String EVENT_CHANNEL_NAME = "com.herald_flutter.eventChannel";
    private static final String method = "initialPayload";
    private static final String removePeer = "removePeer";

    Handler handler = new Handler();
    Runnable runnable;

    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private PayloadDataSupplier supplier;
    private Object _payloadData;
    private int _uuid;
    private int _illnessStatusCode;
    private String _date;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), METHOD_CHANNEL_NAME)
                .setMethodCallHandler((call, result) -> {
                    if (call.method.equals("sendToBackground")) {
                        moveTaskToBack(true);
                        result.success(null);
                    } else if (call.method.equals(method)) {
                        _payloadData = call.arguments;
                        _uuid = call.argument("uuid");
                        _illnessStatusCode = call.argument("illnessStatusCode");
                        _date = call.argument("date");

                        Date date = null;
                        try {
                            date = formatter.parse(_date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        io.heraldprox.herald.sensor.datatype.Date correctDate = new io.heraldprox.herald.sensor.datatype.Date(date);
                        supplier = TestService.instance.payloadDataSupplier;
                        supplier.setIdentifier(_uuid);
                        supplier.setIllnessStatusCode(_illnessStatusCode);
                        supplier.setDate(correctDate);

                        Log.e("PAYLOADDATA", String.valueOf(_payloadData));
                        Log.e("resultsFromFlutter", _uuid + "   " + _illnessStatusCode + "   " + correctDate);
                        result.success(null);
                    } else if (call.method.equals(removePeer)) {
                        _uuid = call.argument("uuid");
                        TestService.instance.distanceEstimator.removeModel(_uuid);
                        result.success(null);
                    } else {
                        result.notImplemented();
                    }
                });

        new EventChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), EVENT_CHANNEL_NAME).setStreamHandler(
                new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object arguments, EventChannel.EventSink events) {
                        TestService.instance.onListen(arguments, events);
                    }
                    @Override
                    public void onCancel(Object arguments) {
                        TestService.instance.onCancel(arguments);
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
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void initService() {
        Context context = getApplicationContext();
        Intent startIntent = new Intent(context, TestService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(startIntent);
        } else {
            context.startService(startIntent);
        }
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

    /**
     * REQUIRED : Request application permissions for sensor operation.
     */
    private void requestPermissions() {
        // Check and request permissions
        final List<String> requiredPermissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            requiredPermissions.add(BLUETOOTH_CONNECT);
            requiredPermissions.add(BLUETOOTH_ADVERTISE);
            requiredPermissions.add(BLUETOOTH_SCAN);
        }
        requiredPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        //     requiredPermissions.add(Manifest.permission.FOREGROUND_SERVICE);
        // }
        // requiredPermissions.add(Manifest.permission.WAKE_LOCK);

        final String[] requiredPermissionsArray = requiredPermissions.toArray(new String[0]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(requiredPermissionsArray, permissionRequestCode);
        } else {
            ActivityCompat.requestPermissions(this, requiredPermissionsArray, permissionRequestCode);
        }
    }

    /**
     * REQUIRED : Handle permission results.
     * 
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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
                    initService();  
                }
            }

            if (!permissionsGranted) {
                Log.e(tag, "Application does not have all required permissions to start (permissions="
                        + Arrays.asList(permissions) + ")");
            }
        }
    }
}
