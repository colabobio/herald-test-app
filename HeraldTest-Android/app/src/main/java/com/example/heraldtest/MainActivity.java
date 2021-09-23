package com.example.heraldtest;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import cesarferreira.androiduniquedeviceid.UniqueDeviceIdProvider;
import io.heraldprox.herald.sensor.SensorArray;
import io.heraldprox.herald.sensor.SensorDelegate;
import io.heraldprox.herald.sensor.ble.BLESensorConfiguration;
import io.heraldprox.herald.sensor.datatype.Date;
import io.heraldprox.herald.sensor.datatype.ImmediateSendData;
import io.heraldprox.herald.sensor.datatype.LegacyPayloadData;
import io.heraldprox.herald.sensor.datatype.Location;
import io.heraldprox.herald.sensor.datatype.PayloadData;
import io.heraldprox.herald.sensor.datatype.Proximity;
import io.heraldprox.herald.sensor.datatype.SensorState;
import io.heraldprox.herald.sensor.datatype.SensorType;
import io.heraldprox.herald.sensor.datatype.TargetIdentifier;
import io.heraldprox.herald.sensor.datatype.TimeInterval;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements SensorDelegate {
    /// REQUIRED: Unique permission request code, used by requestPermission and onRequestPermissionsResult.
    private final static int permissionRequestCode = 1249951875;
    private boolean foreground = false;

    private final static String tag = TestApplication.class.getName();


    public HashMap<Integer, PeerInfo> currentPeers = null;

    public final static String USER_CHANNEL_ID = "com.example.heraldtest.user_notifications";
    public final static String SERVICE_CHANNEL_ID = "com.example.heraldtest.service_notifications";

    private final BroadcastReceiver statusChangedReceiver = TestBroadcast.statusChangeReceived(this::updateStatus);
    private final BroadcastReceiver statusDetectedReceiver = TestBroadcast.peerDetectReceived(this::updatePeers);

    public static IllnessStatusPayloadDataSupplier payloadDataSupplier;

    public SensorArray sensor = null;

    public int identifier(Context context) {
        // TODO for persistence between app restarts, make the 'random' section a check
        //      for a text file value. If no text file, generate random and use. If file
        //      exists, load the value. Otherwise the ID will change on phone restart!
        // Unique UUID from app
        // iOS: https://developer.apple.com/documentation/uikit/uidevice/1620059-identifierforvendor

        UniqueDeviceIdProvider uniqueID = new UniqueDeviceIdProvider(context);
        return uniqueID.getUniqueId().hashCode();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // REQUIRED : Ensure app has all required permissions
        requestPermissions();

        payloadDataSupplier = new IllnessStatusPayloadDataSupplier(identifier(this));

        // The more frequent this is, the more Bluetooth payload transfer failures will result
        // This value DOES NOT slow down INITIAL / new in range payload exchange! That's always ASAP.
        BLESensorConfiguration.payloadDataUpdateTimeInterval = TimeInterval.minutes(1);
        //        BLESensorConfiguration.serviceUUID = // any valid id... this allow us to have multiple teams playing in tghe same area
        // and not interfering each other

        sensor = new SensorArray(getApplicationContext(), payloadDataSupplier);


        // Add appDelegate as listener for detection events for logging and start sensor
        sensor.add(this);
        sensor.start();
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentPeers = new HashMap<>();

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
            status.setText(MainActivity.payloadDataSupplier.getIdentifier() + ":" +
                           MainActivity.payloadDataSupplier.getStatus());
        }
    }

    protected void updatePeers() {
        if (TestApplication.instance != null) {
            EditText peers = findViewById(R.id.peers);
            String txt = "";
            for (Integer id: currentPeers.keySet()) {
                PeerInfo info = currentPeers.get(id);
                txt += "->" + id + ":" + info.status + ":" + info.getRSSI() + "\n";
            }
            peers.setText(txt);
        }
    }

    @Override
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull TargetIdentifier targetIdentifier) {
        Log.i(tag, sensorType.name() + ",payloadData=" + ",targetIdentifier=" + targetIdentifier);
    }

    @Override
    // called every time there is a new payload from the peer
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull PayloadData payloadData, @NonNull @NotNull TargetIdentifier targetIdentifier) {
        Log.i(tag, sensorType.name() + ",payloadData=" + payloadData.shortName() + ",targetIdentifier=" + targetIdentifier);
        parsePayload("didRead", sensorType, payloadData, null, targetIdentifier);
    }

    @Override
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull ImmediateSendData immediateSendData, @NonNull @NotNull TargetIdentifier targetIdentifier) {
        Log.i(tag, sensorType.name() + ",immediateSendData=" + immediateSendData.toString() + ",targetIdentifier=" + targetIdentifier);
//        parsePayload("didShare", sensorType, sensor.payloadData(), targetIdentifier);
    }

    @Override
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull List<PayloadData> list, @NonNull @NotNull TargetIdentifier targetIdentifier) {
//        final List<String> payloads = new ArrayList<>(list.size());
//        for (PayloadData payloadData : list) {
//            payloads.add(payloadData.shortName());
//        }
//        Log.i(tag, sensorType.name() + ",list=" + payloads.toString() + ",targetIdentifier=" + targetIdentifier);
//        for (PayloadData payloadData : list) {
//            parsePayload("didShare", sensorType, payloadData, targetIdentifier);
//        }
    }

    @Override
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull Proximity proximity, @NonNull @NotNull TargetIdentifier targetIdentifier) {
        Log.i(tag, sensorType.name() + ",proximity=" + proximity.toString() + ",targetIdentifier=" + targetIdentifier);
    }

    @Override
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull Location location) {
        Log.i(tag, sensorType.name() + ",location=" + location.toString());
    }

    @Override
    // When there is payload and there is a proximity leading :-)
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull Proximity proximity, @NonNull @NotNull TargetIdentifier targetIdentifier, @NonNull @NotNull PayloadData payloadData) {
        // proximity.value = this is RSSI (signed integer, int8)! 25 values for a standard model...
        Log.i(tag, sensorType.name() + ",proximity=" + proximity.toString() + ",targetIdentifier=" + targetIdentifier + ",payloadData=" + payloadData.shortName());
        parsePayload("didRead", sensorType, payloadData, proximity, targetIdentifier);
    }

    @Override
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull SensorState sensorState) {
        Log.i(tag, sensorType.name() + ",sensorState=" + sensorState.toString());
    }


    private void parsePayload(String source, SensorType sensor, PayloadData payloadData, Proximity proximity, TargetIdentifier fromTarget) {
        String service = "herald";
        String parsedPayload = payloadData.shortName();
        if (payloadData instanceof LegacyPayloadData) {
            final LegacyPayloadData legacyPayloadData = (LegacyPayloadData) payloadData;
            if (legacyPayloadData.service == null) {
                service = "null";
                parsedPayload = payloadData.hexEncodedString();
            } else if (legacyPayloadData.service == BLESensorConfiguration.interopOpenTraceServiceUUID) {
                service = "opentrace";
                parsedPayload = new String(legacyPayloadData.value);
            } else if (legacyPayloadData.service == BLESensorConfiguration.interopAdvertBasedProtocolServiceUUID) {
                service = "advert";
                parsedPayload = payloadData.hexEncodedString();
            } else {
                service = "unknown|" + legacyPayloadData.service.toString();
                parsedPayload = payloadData.hexEncodedString();
            }
            Log.i(tag, "RECEIVED PAYLOAD ------> " + parsedPayload);
            TestBroadcast.triggerPeerDetect();
        } else {
            // Likely an Illness payload
            try {
                int identifier = IllnessStatusPayloadDataSupplier.getIdentifierFromPayload(payloadData);
                IllnessStatus status = IllnessStatusPayloadDataSupplier.getIllnessStatusFromPayload(payloadData);

                Log.i(tag, "Status of individual with ID: " + identifier + " is " + status.toString());

                PeerInfo info = currentPeers.get(identifier);
                if (info == null) {
                    info = new PeerInfo();
                    currentPeers.put(identifier, info);
                }
                info.status = status;
                if (proximity != null) {
                    info.addRSSI(proximity.value);
                    Log.i(tag, "RSSI value: " + proximity.value);
                }
                if (-55 < info.getRSSI()) {
                    // not in contact anymore, remove
                    currentPeers.remove(identifier);
                }

                // TODO other stuff with IllnessStatus and identifier here. E.g. display on the UI
                if (new Date().getTime() - info.lastSeen.getTime() >= (60 * 5)) {
                    currentPeers.remove(identifier);
                }

                TestBroadcast.triggerPeerDetect();
            } catch (Exception e) {
                Log.e(tag, "Error parsing payload data", e);
            }
        }
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