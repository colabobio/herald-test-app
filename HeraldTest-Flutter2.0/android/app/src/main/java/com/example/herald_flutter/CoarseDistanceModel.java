package com.example.herald_flutter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.heraldprox.herald.sensor.analysis.aggregates.Median;
import io.heraldprox.herald.sensor.analysis.sampling.Aggregate;
import io.heraldprox.herald.sensor.analysis.sampling.Sample;
import io.heraldprox.herald.sensor.data.ConcreteSensorLogger;
import io.heraldprox.herald.sensor.data.SensorLogger;
import io.heraldprox.herald.sensor.datatype.DoubleValue;

public class CoarseDistanceModel <T extends DoubleValue> implements Aggregate<T> {
    private final SensorLogger logger = new ConcreteSensorLogger("Analysis", "PiecewiseDistanceModel");
    private final Median<T> median = new Median<>();

    private SimpleKalmanFilter kalman;
    private Double kalmanRssi;

    private int phoneCode;    
    private int thresholds[][] = {
      {-55, -65, -75}, // Thresholds for code = 0 (iPhone)
      {-65, -75, -85}  // Thresholds for code = 1 (Android phone)
    };

    public CoarseDistanceModel() {
        this(0);
    }

    public CoarseDistanceModel(final int phoneCode) {
        this.phoneCode = phoneCode;
        this.kalman = new SimpleKalmanFilter(1, 1, 0.01);
    }

    public void setParameters(final int phoneCode) {
        this.phoneCode = phoneCode;
    }

    @Override
    public int runs() {
        return 1;
    }

    @Override
    public void beginRun(final int thisRun) {
        median.beginRun(thisRun);
    }

    @Override
    public void map(@NonNull final Sample<T> value) {
        median.map(value);
    }

    @Nullable
    @Override
    public Double reduce() {
        final Double medianOfRssi = medianOfRssi();
        if (null == medianOfRssi) {
            logger.debug("reduce, medianOfRssi is null");
            return null;
        }
        kalmanRssi = kalman.updateEstimate(medianOfRssi);
        if (null == kalmanRssi) {
            logger.debug("reduce, kalmanRssi is null");
            return null;
        }

        if (phoneCode < thresholds.length) {
            if (thresholds[phoneCode][0] < kalmanRssi) {
                return 0.5; // Immediate [0, 1]
            } else if (thresholds[phoneCode][1] < kalmanRssi) {
                return 1.5; // Close [1, 2]
            } else if (thresholds[phoneCode][2] < kalmanRssi) {
                return 3.5; // Medium [2, 5]
            } else {
                return 8.0; // Far (more than 5)
            }
        } else {
            return null;
        }
    }

    @Override
    public void reset() {
        median.reset();
    }

    @Nullable
    public Double medianOfRssi() {
        return median.reduce();
    }

    @Nullable
    public Double kalmanOfRssi() {
        return kalmanRssi;
    }
}
