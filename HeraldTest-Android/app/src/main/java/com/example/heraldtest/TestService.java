package com.example.heraldtest;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
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

public class TestService extends Service implements SensorDelegate {
    private static final String tag = "TestService";

    private static final int FOREGROUND_NOTIFICATION_ID = 133;
    private static final int TIME_STEP = 2;

    public static TestService instance;

    public String id;
    public String state;

    private int time0;

    private Handler handler = new Handler();
    private Runnable runnable;


    public HashMap<Integer, PeerInfo> currentPeers = null;
    public IllnessStatusPayloadDataSupplier payloadDataSupplier;
    public SensorArray sensor = null;

    public int RSSI_THRESHOLD = -30;

    private String uniqueID = null;
    private final static String PREF_UNIQUE_ID = "PREF_UNIQUE_DEVICE_ID";
    private String getUniqueId(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null || uniqueID == "") {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.apply();
            }
        }
        return uniqueID;
    }

    public TestService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        currentPeers = new HashMap<>();
        goToForeground();
        initState();
        initSensor();
        updateLoop();
    }

    public int identifier(Context context) {
        // TODO for persistence between app restarts, make the 'random' section a check
        //      for a text file value. If no text file, generate random and use. If file
        //      exists, load the value. Otherwise the ID will change on phone restart!
        // Unique UUID from app
        // iOS: https://developer.apple.com/documentation/uikit/uidevice/1620059-identifierforvendor

        return getUniqueId(context).hashCode();
    }

    private void initSensor() {
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
        payloadDataSupplier.setStatus(new IllnessStatus(IllnessStatusCode.getRandomStatus() ,new Date()));
//        TestApplication.payloadDataSupplier.payload(new Data(state));
    }

    private void updateLoop() {
        updateState();

        Log.i(tag, "in update loop");

        runnable = () -> handler.post(TestService.this::updateLoop);
        handler.postDelayed(runnable, TIME_STEP * 1000);

        for (Map.Entry<Integer, PeerInfo> pair: currentPeers.entrySet()){
            if (new Date().getTime() - pair.getValue().lastSeen.getTime() >= (60 * 5L * 1000)) {
                currentPeers.remove(pair.getKey());
            }
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
                if (RSSI_THRESHOLD < info.getRSSI() && 10 < info.data.size()) {
                    // not in contact anymore, remove
                    currentPeers.remove(identifier);
                }

                // TODO other stuff with IllnessStatus and identifier here. E.g. display on the UI

                TestBroadcast.triggerPeerDetect();
            } catch (Exception e) {
                Log.e(tag, "Error parsing payload data", e);
            }
        }
    }
}