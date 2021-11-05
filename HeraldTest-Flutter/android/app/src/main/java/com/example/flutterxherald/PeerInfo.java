package com.example.flutterxherald;

import java.util.ArrayList;
import io.heraldprox.herald.sensor.datatype.Date;

// https://github.com/broadinstitute/operation-outbreak/issues/163
public class PeerInfo {
    ArrayList<Double> data;
    IllnessStatus status;
    Date lastSeen;
    int updateCount;

    PeerInfo() {
        this.data = new ArrayList<>();
        this.status = new IllnessStatus(IllnessStatusCode.susceptible, new Date());
        this.lastSeen = new Date();
        this.updateCount = 0;
    }

    void setStatus(IllnessStatus status) {
        this.status = status;
        this.lastSeen = new Date();
        this.updateCount += 1;
    }

    void addRSSI(double value) {
        if (-100 <= value && value < 0) {
            data.add(value);
            if (25 < data.size()) {
                data.remove(0);
            }
        }

    }

    double getRSSI() {
        if (0 < data.size()) {
            double sum = 0;
            for (Double v: data) sum += v;
            return sum / data.size();
        } else {
            return -50;
        }
    }
}