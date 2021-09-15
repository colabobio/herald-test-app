package com.example.heraldtest;

import io.heraldprox.herald.sensor.datatype.RSSI;

// https://github.com/broadinstitute/operation-outbreak/issues/163
public class PeerInfo {
    final SampleList<RSSI> srcData = new SampleList<>(25);
    IllnessStatus status;

    void addRSSI(double value) {
        // ...
    }
}
