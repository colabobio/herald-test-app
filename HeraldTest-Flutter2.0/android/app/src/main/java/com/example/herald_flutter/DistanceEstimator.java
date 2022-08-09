package com.example.herald_flutter;

import androidx.annotation.NonNull;

import io.heraldprox.herald.sensor.datatype.Date;
import io.heraldprox.herald.sensor.datatype.RSSI;
import io.heraldprox.herald.sensor.analysis.sampling.Sample;
import io.heraldprox.herald.sensor.analysis.sampling.SampleList;
import io.heraldprox.herald.sensor.datatype.TimeInterval;
import io.flutter.Log;

import java.util.Hashtable;
import java.util.Map;

// Note: "model" is used both for PiecewiseDistanceModel and ModelWrapper.

public class DistanceEstimator { 
    private Map<Integer, ModelWrapper> modelMap;
    private static final String tag = "DistanceEstimator";

    public DistanceEstimator() {
        modelMap = new Hashtable<Integer, ModelWrapper>();
    }

    // Removes the model associated with the given UUID
    public void removeModel(int UUID) {
        Log.i(tag, "Removed UUID: " + UUID);
        modelMap.remove(UUID);
    }
    
    // Creates a model for a given UUID
    private ModelWrapper createModel(int UUID) {
        ModelWrapper model = new ModelWrapper();
        modelMap.put(UUID, model);
        return model;
    }

    // Adds a RSSI value to the model handling the given UUID (will create a new model if needed)
    public void addRSSI(int UUID, Double rssi) {
        Sample<RSSI> sample = new Sample(new RSSI(rssi));
        ModelWrapper model = modelMap.get(UUID);
        if (model == null){
            model = createModel(UUID);
        }
        model.addSample(sample);
    }

    public Double getDistance(int UUID) {
        ModelWrapper model = modelMap.get(UUID);
        return model.getDistance();
    }

    private class ModelWrapper {

        Date lastUpdated;
        Double lastDistance;
        private SimpleKalmanFilter filter;
        private PiecewiseDistanceModel model;
        private SampleList<RSSI> window;
        int minimumWindowSize;
        int maximumWindowSize;
        TimeInterval smoothingWindow;

        public ModelWrapper() {
            lastUpdated = new Date();
            minimumWindowSize = 25;
            maximumWindowSize = 100;
            smoothingWindow = new TimeInterval(60);
            window = new SampleList<RSSI>(maximumWindowSize);
            model = new PiecewiseDistanceModel();
            filter = new SimpleKalmanFilter(1, 1, 0.03);
        }

        public void addSample(Sample<RSSI> sample) {
            window.push(sample);
        }

        public Double getDistance() {
            Date timeNow = new Date();
            // Remove stale samples
            window.clearBeforeDate(new Date(timeNow.secondsSinceUnixEpoch() - smoothingWindow.value));

            if (window.size() < minimumWindowSize) {
                // Not enough samples
                return null;
            }

            model.reset();
            for (Sample<RSSI> sample : window) {
                model.map(sample);
            }

            Double distanceMeasurement = model.reduce();
            Double distanceEstimation = filter.updateEstimate(distanceMeasurement);
            return distanceEstimation;
        }
    }
}
