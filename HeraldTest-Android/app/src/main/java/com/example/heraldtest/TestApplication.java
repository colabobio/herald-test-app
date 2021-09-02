package com.example.heraldtest;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;
import io.heraldprox.herald.sensor.PayloadDataSupplier;
import io.heraldprox.herald.sensor.SensorArray;
import io.heraldprox.herald.sensor.SensorDelegate;
import io.heraldprox.herald.sensor.ble.BLESensorConfiguration;
import io.heraldprox.herald.sensor.datatype.ImmediateSendData;
import io.heraldprox.herald.sensor.datatype.LegacyPayloadData;
import io.heraldprox.herald.sensor.datatype.Location;
import io.heraldprox.herald.sensor.datatype.PayloadData;
import io.heraldprox.herald.sensor.datatype.Proximity;
import io.heraldprox.herald.sensor.datatype.SensorState;
import io.heraldprox.herald.sensor.datatype.SensorType;
import io.heraldprox.herald.sensor.datatype.TargetIdentifier;
import io.heraldprox.herald.sensor.datatype.TimeInterval;
import io.heraldprox.herald.sensor.payload.test.TestPayloadDataSupplier;

public class TestApplication extends Application implements SensorDelegate {
    private final static String tag = TestApplication.class.getName();

    public static TestApplication instance;
    private static boolean activityVisible = false;

    private int identifier() {
        final String text = Build.MODEL + ":" + Build.BRAND;
        return text.hashCode();
    }

    public static PayloadDataSupplier payloadDataSupplier = new TestPayloadDataSupplier(instance.identifier());

    public SensorArray sensor = null;

    public ArrayList<String> peerStatus = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        BLESensorConfiguration.payloadDataUpdateTimeInterval = TimeInterval.minutes(1);

        sensor = new SensorArray(getApplicationContext(), payloadDataSupplier);


        // Add appDelegate as listener for detection events for logging and start sensor
        sensor.add(this);

        peerStatus = new ArrayList<>();
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

    @Override
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull TargetIdentifier targetIdentifier) {
        Log.i(tag, sensorType.name() + ",payloadData=" + ",targetIdentifier=" + targetIdentifier);
    }

    @Override
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull PayloadData payloadData, @NonNull @NotNull TargetIdentifier targetIdentifier) {
        Log.i(tag, sensorType.name() + ",payloadData=" + payloadData.shortName() + ",targetIdentifier=" + targetIdentifier);
        parsePayload("didRead", sensorType, payloadData, targetIdentifier);
    }

    @Override
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull ImmediateSendData immediateSendData, @NonNull @NotNull TargetIdentifier targetIdentifier) {
        Log.i(tag, sensorType.name() + ",immediateSendData=" + immediateSendData.toString() + ",targetIdentifier=" + targetIdentifier);
        parsePayload("didShare", sensorType, sensor.payloadData(), targetIdentifier);
    }

    @Override
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull List<PayloadData> list, @NonNull @NotNull TargetIdentifier targetIdentifier) {
        final List<String> payloads = new ArrayList<>(list.size());
        for (PayloadData payloadData : list) {
            payloads.add(payloadData.shortName());
        }
        Log.i(tag, sensorType.name() + ",list=" + payloads.toString() + ",targetIdentifier=" + targetIdentifier);
        for (PayloadData payloadData : list) {
            parsePayload("didShare", sensorType, payloadData, targetIdentifier);
        }
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
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull Proximity proximity, @NonNull @NotNull TargetIdentifier targetIdentifier, @NonNull @NotNull PayloadData payloadData) {
        Log.i(tag, sensorType.name() + ",proximity=" + proximity.toString() + ",targetIdentifier=" + targetIdentifier + ",payloadData=" + payloadData.shortName());
    }

    @Override
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull SensorState sensorState) {
        Log.i(tag, sensorType.name() + ",sensorState=" + sensorState.toString());
    }


    private void parsePayload(String source, SensorType sensor, PayloadData payloadData, TargetIdentifier fromTarget) {
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
            peerStatus.add(parsedPayload);
            Log.i(tag, "RECEIVED PAYLOAD ------> " + parsedPayload);
            TestBroadcast.triggerPeerDetect();
        }
    }
}
