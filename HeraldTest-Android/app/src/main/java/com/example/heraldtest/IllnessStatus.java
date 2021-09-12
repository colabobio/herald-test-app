package com.example.heraldtest;

import io.heraldprox.herald.sensor.datatype.Data;
import io.heraldprox.herald.sensor.datatype.Date;
import io.heraldprox.herald.sensor.datatype.PayloadData;
import io.heraldprox.herald.sensor.datatype.UInt64;
import io.heraldprox.herald.sensor.datatype.UInt8;

/**
 * Represents an individual's illness status for transmission over the wire.
 * Includes a static method for parsing, and an instance method for toString.
 */
public class IllnessStatus {
    IllnessStatusCode status;
    Date since;

    /**
     * Constructor to set all fields.
     *
     * @param status Illness Status
     * @param since When this status began
     */
    public IllnessStatus(IllnessStatusCode status,Date since) {
        this.status = status;
        this.since = since;
    }

    /**
     * Updates all fields.
     *
     * @param newStatus The new status, with since set to the current time
     */
    public void update(IllnessStatusCode newStatus) {
        update(newStatus, new Date());
    }

    /**
     * Updates all fields
     *
     * @param newStatus New illness status
     * @param newSince When this status began
     */
    public void update(IllnessStatusCode newStatus,Date newSince) {
        status = newStatus;
        since = newSince;
    }

    /**
     * Returns the code associated with this status.
     *
     * @return Status code instance
     */
    public IllnessStatusCode getCode() {
        return status;
    }

    /**
     * Returns the date the current status began
     *
     * @return The date this status began
     */
    public Date getSince() {
        return since;
    }

    /**
     * Return's a Herald PayloadData object for transmission over the wire/Bluetooth
     *
     * @return A valid PayloadData object with 9 bytes of data
     */
    public PayloadData toPayload() {
        PayloadData result = new PayloadData();
        result.append(new UInt64(since.secondsSinceUnixEpoch())); // bytes at indexes 0 - 7
        result.append(new UInt8(status.getValue())); // byte at index 8 (9th byte)
        return result;
    }

    /**
     * Returns a string describing this instance
     *
     * @return A descriptive string including status and since date
     */
    public String toString() {
        return "Status: " + status.toString() + ", since (epoch): " + since.toString();
    }

    /**
     * Parses a raw payload. Returns null if parse failed rather than throwing.
     *
     * @param raw PayloadData or Data from Herald to be parsed
     * @return null or IllnessStatus from remote device
     */
    public static IllnessStatus fromPayload(Data raw) {
        // TODO reinstate this check once below bug fixed
        // Note: No size() function (or length() equivalent) in v2.0 java API
        // See bug: https://github.com/theheraldproject/herald-for-android/issues/225
//        if (raw.size() != 9) {
//            return null; // parse error
//        }
        Date s = new Date(raw.uint64(0).value);
        IllnessStatusCode code = IllnessStatusCode.valueOf(raw.uint8(8).value);
        return new IllnessStatus(code,s);
    }
}
