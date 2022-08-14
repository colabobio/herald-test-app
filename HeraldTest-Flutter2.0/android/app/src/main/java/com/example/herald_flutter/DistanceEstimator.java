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
    private ModelWrapper createModel(int UUID, int phone) {
        ModelWrapper model = new ModelWrapper(phone);
        modelMap.put(UUID, model);
        return model;
    }

    // Adds a RSSI value to the model handling the given UUID (will create a new model if needed)
    public void addRSSI(int UUID, Double rssi, int phone) {
        Sample<RSSI> sample = new Sample(new RSSI(rssi));
        ModelWrapper model = modelMap.get(UUID);
        if (model == null) {
            model = createModel(UUID, phone);
        }
        model.addSample(sample);
    }

    public Double getDistance(int UUID) {
        ModelWrapper model = modelMap.get(UUID);
        if (model != null) {
            return model.getDistance();
        } else {
            return null;
        }        
    }

    public int getSampleCount(int UUID) {
        ModelWrapper model = modelMap.get(UUID);
        if (model != null) {
            return model.getSampleCount();
        } else {
            return 0;
        }
    }

    public Double getMedianRSSI(int UUID) {
        ModelWrapper model = modelMap.get(UUID);
        if (model != null) {
            return model.getMedianRSSI();
        } else {
            return null;
        }          
    }

    public Double getKalmandRSSI(int UUID) {
        ModelWrapper model = modelMap.get(UUID);
        if (model != null) {
            return model.getKalmanRSSI();
        } else {
            return null;
        }          
    }

    private class ModelWrapper {

        Date lastUpdated;
        Double lastDistance;
        private CoarseDistanceModel model;
        private SampleList<RSSI> window;
        int minimumWindowSize;
        int maximumWindowSize;
        TimeInterval smoothingWindow;

        public ModelWrapper(int phoneCode) {
            lastUpdated = new Date();
            minimumWindowSize = 10;
            maximumWindowSize = 50;
            smoothingWindow = new TimeInterval(60);
            window = new SampleList<RSSI>(maximumWindowSize);
            model = new CoarseDistanceModel(phoneCode);
        }

        public void addSample(Sample<RSSI> sample) {
            window.push(sample);
        }

        public int getSampleCount() {
            return window.size();
        }

        public Double getMedianRSSI() {
            return model.medianOfRssi();
        }

        public Double getKalmanRSSI() {
            return model.kalmanOfRssi();
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

            Double distanceBucket = model.reduce();
            return distanceBucket;
        }
    }
}
