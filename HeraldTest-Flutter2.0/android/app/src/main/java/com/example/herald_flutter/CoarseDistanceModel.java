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

    private int phoneCode;

    public CoarseDistanceModel() {
        this(0);
    }

    public CoarseDistanceModel(final int phoneCode) {
        this.phoneCode = phoneCode;
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

        if (phoneCode == 0) {
            // An iPhone
            if (-55 < medianOfRssi) return 0.5; // Very close [0, 1]
            else if (-65 < medianOfRssi) return 1.5; // Close [1, 2]
            else if (-75 < medianOfRssi) return 3.5; // Relatively close [2, 5]
            else return 8.0; // Far (more than 5)
        } else if (phoneCode == 1) {
            // An Android phone
            if (-65 < medianOfRssi) return 0.5; // Very close [0, 1]
            else if (-75 < medianOfRssi) return 1.5; // Close [1, 2]
            else if (-85 < medianOfRssi) return 3.5; // Relatively close [2, 5]
            else return 8.0; // Far (more than 5)
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
}
