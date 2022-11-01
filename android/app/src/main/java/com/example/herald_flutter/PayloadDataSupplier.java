package com.example.herald_flutter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.heraldprox.herald.sensor.Device;
import io.heraldprox.herald.sensor.datatype.Data;
import io.heraldprox.herald.sensor.datatype.Date;
import io.heraldprox.herald.sensor.datatype.PayloadData;
import io.heraldprox.herald.sensor.datatype.PayloadTimestamp;
import io.heraldprox.herald.sensor.datatype.UInt64;
import io.heraldprox.herald.sensor.datatype.UInt8;
import io.heraldprox.herald.sensor.payload.DefaultPayloadDataSupplier;

public class PayloadDataSupplier extends DefaultPayloadDataSupplier {

    protected int identifier;
    protected int illnessStatusCode;
    protected Date date;
    protected int phoneCode = 1; // 0 for iPhone, 1 for Android

    void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    void setIllnessStatusCode(int illnessStatusCode) {
        this.illnessStatusCode = illnessStatusCode;
    }

    void setDate(Date date) {
        this.date = date;
    }

    public PayloadDataSupplier(int identifier, int illnessStatusCode, Date date) {
        this.identifier = identifier;
        this.illnessStatusCode = illnessStatusCode;
        this.date = date;
    }

    public PayloadData toPayload() {
        PayloadData result = new PayloadData();
        result.append(new UInt64(date.secondsSinceUnixEpoch())); // bytes at indexes 0 - 7
        result.append(new UInt8(illnessStatusCode)); // byte at index 8 (9th byte)
        result.append(new UInt8(phoneCode)); // byte at index 9 (10th byte)
        return result;
    }

    @NonNull
    @Override
    public PayloadData payload(@NonNull PayloadTimestamp payloadTimestamp, @Nullable Device device) {
        PayloadData result = new PayloadData();
        result.append(new UInt64(identifier));
        result.append(toPayload());
        return result;
    }

    static int getIdentifierFromPayload(PayloadData identifierPayload) {
        return (int) identifierPayload.uint64(0).value; // use first 8 bytes only
    }

    static statusPayloadResults getIllnessStatusFromPayload(PayloadData statusPayload) {
        return fromPayload(statusPayload.subdata(8));
        // don't include first 8 bytes, which contain the identifier
    }

    public static statusPayloadResults fromPayload(Data raw) {

        Date date = new Date(raw.uint64(0).value);
        int illnessStatusCode = raw.subdata(8).uint8(0).value;
        int phoneCode = raw.subdata(9).uint8(0).value;

        return new statusPayloadResults(date, illnessStatusCode, phoneCode);
    }
}

final class statusPayloadResults {
    private final Date date;
    private final int illnessStatusCode;
    private final int phoneCode; // 0 for iPhone, 1 for Android

    public statusPayloadResults(Date date, int illnessStatusCode, int phoneCode) {
        this.date = date;
        this.illnessStatusCode = illnessStatusCode;
        this.phoneCode = phoneCode;
    }

    public Date getDate() {
        return date;
    }

    public int getIllnessStatusCode() {
        return illnessStatusCode;
    }

    public int getPhoneCode() {
        return phoneCode;
    }
}