package com.example.heraldtest;

import java.util.ArrayList;

import io.heraldprox.herald.sensor.analysis.sampling.SampleList;
import io.heraldprox.herald.sensor.datatype.RSSI;

// https://github.com/broadinstitute/operation-outbreak/issues/163
public class PeerInfo {
    ArrayList<Double> data = new ArrayList<>();
    IllnessStatus status;

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
            return -100;
        }
    }
}