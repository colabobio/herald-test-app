package com.example.herald_flutter;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.flutter.plugin.common.EventChannel;
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

public class TestService extends Service implements SensorDelegate, EventChannel.StreamHandler {

    private static final String tag = "TestService";
    private static final String SIMULATION_ID = "8693a908-43cf-44b3-9444-b91c04b83877";

    private static final boolean DISABLE_HERALD = false;

    private static final int FOREGROUND_NOTIFICATION_ID = 133;
    private static final int TIME_STEP = 1;

    public static TestService instance;

    private final Date newTempDate = new Date();
    public PayloadDataSupplier payloadDataSupplier;
    public SensorArray sensor = null;

    private final Handler updateLoopHandler = new Handler();
    private final Handler peersPayloadHandler = new Handler();
    private final Runnable peersPayloadRunnable = this::sendPeersPayload;
    private EventChannel.EventSink peersPayloadEventSink;

    private ConcurrentHashMap<String, Object> storePeersPayload = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        storePeersPayload = new ConcurrentHashMap<>();
        payloadDataSupplier = new PayloadDataSupplier(1234567890, 1, newTempDate);
        goToForeground();
        initSensor();
        updateLoop();
    }

    private void goToForeground() {
        Notification notification = new NotificationCompat.Builder(this, MainActivity.SERVICE_CHANNEL_ID)
                .setContentTitle("Listening to peers").setContentText("Background process to listen to peers")
                .setOngoing(true).build();

        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
    }

    private void updateLoop() {
        Log.i(tag, "in update loop");
        Runnable runnable = () -> updateLoopHandler.post(TestService.this::updateLoop);
        updateLoopHandler.postDelayed(runnable, TIME_STEP * 1000);
    }

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // run until explicitly stopped.
    }

    public TestService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new TestServiceBinder(this);
    }

    private void initSensor() {
        if (DISABLE_HERALD)
            return;

        // The more frequent this is, the more Bluetooth payload transfer failures will
        // result
        // This value DOES NOT slow down INITIAL / new in range payload exchange! That's
        // always ASAP.
        BLESensorConfiguration.payloadDataUpdateTimeInterval = TimeInterval.minutes(1);

        // This allow us to have multiple teams playing in the same area and not
        // interfering each other
        // https://www.uuidgenerator.net/version4
        BLESensorConfiguration.serviceUUID = UUID.fromString(SIMULATION_ID);

        sensor = new SensorArray(getApplicationContext(), payloadDataSupplier);

        // Add appDelegate as listener for detection events for logging and start sensor
        sensor.add(this);
        sensor.start();
    }

    @Override
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull TargetIdentifier targetIdentifier) {
        Log.i(tag, sensorType.name() + ",payloadData=" + ",targetIdentifier=" + targetIdentifier);
    }

    @Override
    // called every time there is a new payload from the peer
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull PayloadData payloadData,
            @NonNull @NotNull TargetIdentifier targetIdentifier) {
        Log.i(tag, sensorType.name() + ",payloadData=" + payloadData.shortName() + ",targetIdentifier="
                + targetIdentifier);
        parsePayload("didRead", sensorType, payloadData, null, targetIdentifier);
    }

    @Override
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull ImmediateSendData immediateSendData,
            @NonNull @NotNull TargetIdentifier targetIdentifier) {
        Log.i(tag, sensorType.name() + ",immediateSendData=" + immediateSendData.toString() + ",targetIdentifier="
                + targetIdentifier);
        // parsePayload("didShare", sensorType, sensor.payloadData(), targetIdentifier);
    }

    @Override
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull List<PayloadData> didShare,
            @NonNull @NotNull TargetIdentifier targetIdentifier) {
        final List<String> payloads = new ArrayList<>(didShare.size());
        for (PayloadData payloadData : didShare) {
            payloads.add(payloadData.shortName());
        }
        Log.i(tag, sensorType.name() + ",list=" + payloads.toString() + ",targetIdentifier=" + targetIdentifier);
        for (PayloadData payloadData : didShare) {
            parsePayload("didShare", sensorType, payloadData, null, targetIdentifier);
        }
    }

    @Override
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull Proximity proximity,
            @NonNull @NotNull TargetIdentifier targetIdentifier) {
        Log.i(tag, sensorType.name() + ",proximity=" + proximity.toString() + ",targetIdentifier=" + targetIdentifier);
    }

    @Override
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull Location location) {
        Log.i(tag, sensorType.name() + ",location=" + location.toString());
    }

    @Override
    // When there is payload and there is a proximity leading :-)
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull Proximity proximity,
            @NonNull @NotNull TargetIdentifier targetIdentifier, @NonNull @NotNull PayloadData payloadData) {
        // proximity.value = this is RSSI (signed integer, int8)! 25 values for a
        // standard model...
        Log.i(tag, sensorType.name() + ",proximity=" + proximity.toString() + ",targetIdentifier=" + targetIdentifier
                + ",payloadData=" + payloadData.shortName());
        parsePayload("didRead", sensorType, payloadData, proximity, targetIdentifier);
    }

    @Override
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull SensorState sensorState) {
        Log.i(tag, sensorType.name() + ",sensorState=" + sensorState.toString());
    }

    private void parsePayload(String source, SensorType sensor, PayloadData payloadData, Proximity proximity,
            TargetIdentifier fromTarget) {
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
        } else {
            // Likely an Illness payload
            try {
                statusPayloadResults statusPayload = PayloadDataSupplier.getIllnessStatusFromPayload(payloadData);
                int identifier = PayloadDataSupplier.getIdentifierFromPayload(payloadData);
                int illnessStatusCode = statusPayload.getIllnessStatusCode();
                Date date = statusPayload.getDate();

                storePeersPayload.put("uuid", identifier);
                storePeersPayload.put("code", illnessStatusCode);
                storePeersPayload.put("date", date.toString());
                if (proximity.value != null) {
                    storePeersPayload.put("rssi", proximity.value);
                }

            } catch (Exception e) {
                Log.e(tag, "Error parsing payload data", e);
            }
        }
    }

    void sendPeersPayload() {
        peersPayloadEventSink.success(storePeersPayload);
        peersPayloadHandler.postDelayed(peersPayloadRunnable, 1000);
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        peersPayloadEventSink = events;
        peersPayloadHandler.post(peersPayloadRunnable);
    }

    @Override
    public void onCancel(Object arguments) {
        peersPayloadEventSink = null;
        peersPayloadHandler.removeCallbacks(peersPayloadRunnable);
    }
}
