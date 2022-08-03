package com.example.herald_flutter;

import androidx.annotation.NonNull;

import io.heraldprox.herald.sensor.analysis.algorithms.distance.SmoothedLinearModel;
import io.heraldprox.herald.sensor.datatype.Date;
import io.heraldprox.herald.sensor.datatype.RSSI;
import io.heraldprox.herald.sensor.analysis.sampling.Sample;
import io.heraldprox.herald.sensor.analysis.sampling.SampleList;
import io.heraldprox.herald.sensor.datatype.TimeInterval;
import io.flutter.Log;



import java.util.Hashtable;
import java.util.Map;

// Note: "model" is used both for SmoothedLinerModel and ModelWrapper.


public class DistanceEstimator { 
    private Map<Integer, ModelWrapper> modelMap;
    private static final String tag = "DistanceEstimator";
    


    public DistanceEstimator(){
        modelMap = new Hashtable<Integer, ModelWrapper>();
    }

    //removes the model associated with the given UUID
    public void removeModel(int UUID){
        Log.i(tag, "Removed UUID: " + UUID);
        modelMap.remove(UUID);
    }

    //creates a model for a given UUID
    private ModelWrapper createModel(int UUID){
        ModelWrapper model = new ModelWrapper();
        modelMap.put(UUID, model);
        return model;
    }

    //adds a RSSI value to the model handling the given UUID (will create a new model if needed)
    public void addRSSI(int UUID, Double rssi){
        Sample<RSSI> sample = new Sample(new RSSI(rssi));
        ModelWrapper model = modelMap.get(UUID);
        if (model == null){
            model = createModel(UUID);
        }
        model.addSample(sample);
    }


    public Double getDistance(int UUID){//TODO deal with not having enough samples to give distance or no model exists
        ModelWrapper model = modelMap.get(UUID);
        return model.getDistance();
    }

    //This class essentially takes the role of SmoothedLinearModelAnalyzer and is heavily influenced by it
    //I made this class since SmoothedLinearModelAnalyzer is not *quite* what is required for this use
    private class ModelWrapper{

        Date lastUpdated;
        Double lastDistance; 
        private SmoothedLinearModel model;
        private SampleList<RSSI> window;
        int maximumWindowSize; //TODO decide where these values go and how they work
        TimeInterval smoothingWindow;


        public ModelWrapper(){
            lastUpdated = new Date();
            maximumWindowSize = 100;
            smoothingWindow = new TimeInterval(10);
            window = new SampleList<RSSI>(maximumWindowSize);
            model = new SmoothedLinearModel();
        }

        public void addSample(Sample<RSSI> sample){
            window.push(sample);
        }

        public Double getDistance(){//TODO fix breaking for very close ranges
            Date timeNow = new Date();
            //remove stale samples
            window.clearBeforeDate(new Date(timeNow.secondsSinceUnixEpoch() - smoothingWindow.value));

            model.reset();
            for(Sample<RSSI> sample : window){
                model.map(sample);
            }
            return model.reduce();
        }
    }

}
