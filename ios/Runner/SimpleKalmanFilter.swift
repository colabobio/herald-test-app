//
//  SimpleKalmanFilter.swift
//  Runner
//
//  Created by Andrés Colubri on 8/9/22.
//

import Foundation

// Simple 1-D Kalman Filter based on the code available at:
// https://github.com/denyssene/SimpleKalmanFilter
public class SimpleKalmanFilter {
    public var errMeasure: Double
    public var errEstimate: Double
    public var q: Double
    public var currentEstimate: Double
    public var lastEstimate: Double
    public var kalmanGain: Double
    
    public init(_ errMeasure: Double, _ errEst: Double, _ pNoise: Double) {
        self.errMeasure = errMeasure
        self.errEstimate = errEst
        self.q = pNoise
        self.currentEstimate = 0
        self.lastEstimate = 0
        self.kalmanGain = 0
    }
    
    public func updateEstimate(_ measure: Double?) -> Double? {
        guard let measureValue = measure else {
            return nil
        }
        
        kalmanGain = errEstimate / (errEstimate + errMeasure)
        currentEstimate = lastEstimate + kalmanGain * (measureValue - lastEstimate)
        errEstimate = (1.0 - kalmanGain) * errEstimate + abs(lastEstimate - currentEstimate) * q
        lastEstimate = currentEstimate
        return currentEstimate
    }
    
    public func setMeasurementError(_ errMeasure: Double) {
        self.errMeasure = errMeasure
    }

    public func setEstimateError(_ errEst: Double) {
        self.errEstimate = errEst
    }
        
    public func setProcessNoise(_ pNoise: Double) {
        self.q = pNoise
    }

    public func getKalmanGain() -> Double {
        return self.kalmanGain
    }
            
    public func getEstimateError() -> Double {
        return self.errEstimate
    }
}
