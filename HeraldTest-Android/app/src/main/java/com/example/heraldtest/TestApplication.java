package com.example.heraldtest;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        // TODO for persistence between app restarts, make the 'random' section a check
        //      for a text file value. If no text file, generate random and use. If file
        //      exists, load the value. Otherwise the ID will change on phone restart!
        // Unique UUID from app
        // iOS: https://developer.apple.com/documentation/uikit/uidevice/1620059-identifierforvendor
        final String text = Build.MODEL + ":" + Build.BRAND + ":" + (new Random()).nextInt();
        return text.hashCode();
    }

    public static IllnessStatusPayloadDataSupplier payloadDataSupplier =
            new IllnessStatusPayloadDataSupplier(instance.identifier());

    public SensorArray sensor = null;

    public ArrayList<String> peerStatus = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // The more frequent this is, the more Bluetooth payload transfer failures will result
        // This value DOES NOT slow down INITIAL / new in range payload exchange! That's always ASAP.
        BLESensorConfiguration.payloadDataUpdateTimeInterval = TimeInterval.minutes(1);
        //        BLESensorConfiguration.serviceUUID = // any valid id... this allow us to have multiple teams playing in tghe same area
                                                       // and not interfering each other

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
    // called every time there is a new payload from the peer
    public void sensor(@NonNull @NotNull SensorType sensorType, @NonNull @NotNull PayloadData payloadData, @NonNull @NotNull TargetIdentifier targetIdentifier) {
        Log.i(tag, sensorType.name() + ",payloadData=" + payloadData.shortName() + ",targetIdentifier=" + targetIdentifier);
        parsePayload("didRead", sensorType, payloadData, targetIdentifier);
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
        } else {
            // Likely an Illness payload
            try {
                int identifier = IllnessStatusPayloadDataSupplier.getIdentifierFromPayload(payloadData);
                IllnessStatus status = IllnessStatusPayloadDataSupplier.getIllnessStatusFromPayload(payloadData);
                Log.i(tag, "Status of individual with ID: " + identifier + " is " + status.toString());

                // TODO other stuff with IllnessStatus and identifier here. E.g. display on the UI

                TestBroadcast.triggerPeerDetect();
            } catch (Exception e) {
                Log.e(tag, "Error parsing payload data", e);
            }
        }
    }
}
