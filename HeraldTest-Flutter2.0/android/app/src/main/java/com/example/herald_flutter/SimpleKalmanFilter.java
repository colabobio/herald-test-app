package com.example.herald_flutter;

import java.lang.Math;

import androidx.annotation.Nullable;

// Simple 1-D Kalman Filter based on the code available at:
// https://github.com/denyssene/SimpleKalmanFilter
public class SimpleKalmanFilter {
    double errMeasure;
    double errEstimate;
    double q;
    double currentEstimate;
    double lastEstimate;
    double kalmanGain;

    public SimpleKalmanFilter(final double errMeasure, final double errEst, final double pNoise) {
        this.errMeasure = errMeasure;
        this.errEstimate = errEst;
        this.q = pNoise;
        this.currentEstimate = 0;
        this.lastEstimate = 0;
        this.kalmanGain = 0;
    }

    @Nullable
    public Double updateEstimate(Double measure) {
        if (measure == null) return null;
        kalmanGain = errEstimate / (errEstimate + errMeasure);
        currentEstimate = lastEstimate + kalmanGain * (measure - lastEstimate);
        errEstimate = (1.0 - kalmanGain) * errEstimate + Math.abs(lastEstimate - currentEstimate) * q;
        lastEstimate = currentEstimate;
        return currentEstimate;     
    }

    public void setMeasurementError(final double errMeasure) {
        this.errMeasure = errMeasure;
    }

    public void setEstimateError(final double errEst) {
        this.errEstimate = errEst;
    }
        
    public void setProcessNoise(final double pNoise) {
        this.q = pNoise;
    }

    public double getKalmanGain() {
        return this.kalmanGain;
    }
            
    public double getEstimateError() {
        return this.errEstimate;
    }        
}
