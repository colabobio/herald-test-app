package com.example.flutterxherald;

import io.heraldprox.herald.sensor.datatype.Date;
import io.heraldprox.herald.sensor.datatype.UInt64;
import io.heraldprox.herald.sensor.datatype.PayloadData;
import io.heraldprox.herald.sensor.datatype.PayloadTimestamp;
import io.heraldprox.herald.sensor.Device;
import io.heraldprox.herald.sensor.payload.DefaultPayloadDataSupplier;

public class IllnessStatusPayloadDataSupplier extends DefaultPayloadDataSupplier {
    protected int identifier;
    protected IllnessStatus status; // set by only constructor

    public IllnessStatusPayloadDataSupplier(int identifier,IllnessStatus initialStatus) {
        this.identifier = identifier;
        status = initialStatus;
    }

    public IllnessStatusPayloadDataSupplier(int identifier) {
        this.identifier = identifier;
        status = new IllnessStatus(IllnessStatusCode.susceptible,new Date());
    }

    public void setStatus(IllnessStatus newStatus) {
        this.status = newStatus;
    }

    public IllnessStatus getStatus() {
        return status;
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    /**
     * Returns a payload to be sent by Herald to nearby devices upon request (Payload Read)
     * @param timestamp The timestamp for which the read was requested. May be null.
     * @param device The virtual nearby Herald device reference (unique per remote requestor). May be null.
     * @return PayloadData instance containing UInt64 dateSince and UInt8 illnessStatus
     */
    public PayloadData payload(@androidx.annotation.NonNull PayloadTimestamp timestamp,
                               @androidx.annotation.Nullable Device device) {
        // Return our status as a series of Bytes in a Data object instance
        // Note: Should cache this if it doesn't change often as a member in order to save processing CPU and thus battery
        PayloadData result = new PayloadData();
        result.append(new UInt64(identifier)); // bytes 0-7
        result.append(status.toPayload()); // bytes 8-16
        return result;
    }

    static int getIdentifierFromPayload(PayloadData illnessPayload) {
        return (int)illnessPayload.uint64(0).value; // use first 8 bytes only
    }

    static IllnessStatus getIllnessStatusFromPayload(PayloadData illnessPayload) {
        return IllnessStatus.fromPayload(illnessPayload.subdata(8)); // don't include first 8 bytes
    }
}
