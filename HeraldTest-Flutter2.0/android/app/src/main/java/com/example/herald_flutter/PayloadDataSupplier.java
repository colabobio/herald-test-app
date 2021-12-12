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
        // don't include first 8 bytes
    }

    public static statusPayloadResults fromPayload(Data raw) {

        Date date = new Date(raw.uint64(0).value);
        int illnessStatusCode = raw.uint8(8).value;

        return new statusPayloadResults(date, illnessStatusCode);
    }
}

final class statusPayloadResults {
    private final Date date;
    private final int illnessStatusCode;

    public statusPayloadResults(Date date, int illnessStatusCode) {
        this.date = date;
        this.illnessStatusCode = illnessStatusCode;
    }

    public Date getDate() {
        return date;
    }

    public int getIllnessStatusCode() {
        return illnessStatusCode;
    }
}