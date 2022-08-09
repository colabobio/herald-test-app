package com.example.herald_flutter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.heraldprox.herald.sensor.analysis.aggregates.Median;
import io.heraldprox.herald.sensor.analysis.sampling.Aggregate;
import io.heraldprox.herald.sensor.analysis.sampling.Sample;
import io.heraldprox.herald.sensor.data.ConcreteSensorLogger;
import io.heraldprox.herald.sensor.data.SensorLogger;
import io.heraldprox.herald.sensor.datatype.DoubleValue;

public class PiecewiseDistanceModel<T extends DoubleValue> implements Aggregate<T> {
    private final SensorLogger logger = new ConcreteSensorLogger("Analysis", "PiecewiseDistanceModel");
    private final Median<T> median = new Median<>();

    // Parameters of the linear interpolation piece
    protected double intercept;
    protected double coefficient;

    // Parameters of the exponential (loss path) model
    protected double distance0;
    protected double rssi0;
    protected double exponent;
        
    public PiecewiseDistanceModel() {
        this(-17.102080, -0.266793, 3.0, 3.0);
    }

    public PiecewiseDistanceModel(final double intercept, final double coefficient, final double distance0, final double exponent) {
        this.intercept = intercept;
        this.coefficient = coefficient;
        this.distance0 = distance0;
        this.exponent = exponent;
        this.rssi0 = (distance0 - intercept) / coefficient;
    }

    public void setParameters(final double intercept, final double coefficient, final double distance0, final double exponent) {
        this.intercept = intercept;
        this.coefficient = coefficient;
        this.distance0 = distance0;
        this.exponent = exponent;
        this.rssi0 = (distance0 - intercept) / coefficient;
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
        if (rssi0 < medianOfRssi) {
            // Exponential estimation at close range
            final Double x = -(medianOfRssi - rssi0) / (10 * exponent);
            final Double distanceInMetres = Math.pow(10, x);
            return distanceInMetres;
        } else {
            // Linear estimation at longer ranges
            final Double distanceInMetres = intercept + coefficient * medianOfRssi;
            if (distanceInMetres <= 0) {
                logger.debug("reduce, out of range (medianOfRssi={},distanceInMetres={})", medianOfRssi, distanceInMetres);
                return null;
            }
            return distanceInMetres;
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

