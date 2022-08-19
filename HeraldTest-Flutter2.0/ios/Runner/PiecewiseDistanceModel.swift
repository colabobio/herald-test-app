//
//  PiecewiseDistanceModel.swift
//  Runner
//
//  Created by Andrés Colubri on 8/9/22.
//

import Foundation
import Herald

public class PiecewiseDistanceModel: Aggregate {
    open override var runs: Int { get { 1 }}
    private var run: Int = 1
    private let median: Median = Median()
    
    // Parameters of the linear interpolation piece
    public var intercept: Double
    public var coefficient: Double
    
    // Parameters of the exponential (loss path) model
    public var distance0: Double
    public var rssi0: Double
    public var exponent: Double
    
    public static let defaultIntercept: Double = -17.102080
    public static let defaultCoefficient: Double = -0.266793

    public static let defaultDistance0: Double = 3.0
    public static let defaultExponent: Double = 3.0
    
    public init(intercept: Double = PiecewiseDistanceModel.defaultIntercept, coefficient: Double = PiecewiseDistanceModel.defaultCoefficient, distance0: Double = PiecewiseDistanceModel.defaultDistance0, defaultExponent: Double = PiecewiseDistanceModel.defaultExponent) {

        self.intercept = intercept
        self.coefficient = coefficient
        
        self.distance0 = distance0
        self.exponent = defaultExponent
        
        self.rssi0 = (distance0 - intercept) / coefficient
    }

    public override func beginRun(thisRun: Int) {
        run = thisRun
        median.beginRun(thisRun: thisRun)
    }
    
    public override func map(value: Sample) {
        median.map(value: value)
    }

    public override func reduce() -> Double? {
        guard let medianOfRssiValue = medianOfRssi() else {
            return nil
        }
        if rssi0 < medianOfRssiValue {
            // Exponential estimation at close range
            let x = -(medianOfRssiValue - rssi0) / (10 * exponent)
            let distanceInMetres = pow(10, x)
            return distanceInMetres
        } else {
            // Linear estimation at longer ranges
            let distanceInMetres = intercept + coefficient * medianOfRssiValue
            guard distanceInMetres >= 0 else {
                return nil
            }
            return distanceInMetres
        }
    }
    
    public override func reset() {
        median.reset()
    }
    
    public func medianOfRssi() -> Double? {
        return median.reduce()
    }
}
