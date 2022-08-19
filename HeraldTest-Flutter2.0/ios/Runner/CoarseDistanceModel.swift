//
//  CoarseDistanceModel.swift
//  Runner
//
//  Created by Andrés Colubri on 8/9/22.
//

import Foundation
import Herald

public class CoarseDistanceModel: Aggregate {
    open override var runs: Int { get { 1 }}
    private var run: Int = 1
    private let median: Median = Median()
    
    var kalman: SimpleKalmanFilter
    var kalmanRssi: Double?
    
    public var phoneCode: Int
    var thresholds: [[Int]] =
    [
      [-55, -65, -75], // Thresholds for code = 0 (iPhone)
      [-65, -75, -85]  // Thresholds for code = 1 (Android phone)
    ]
    
    public static let defaultPhoneCode: Int = 0
    
    public init(phoneCode: Int = CoarseDistanceModel.defaultPhoneCode) {
        self.phoneCode = phoneCode
        self.kalman = SimpleKalmanFilter(1, 1, 0.01)
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
        kalmanRssi = kalman.updateEstimate(medianOfRssiValue)
        guard let krssi = kalmanRssi else {
            return nil
        }
        
        if phoneCode < thresholds.count {
            if thresholds[phoneCode][0] < Int(krssi) {
                return 0.5 // Immediate [0, 1]
            } else if thresholds[phoneCode][1] < Int(krssi) {
                return 1.5 // Close [1, 2]
            } else if thresholds[phoneCode][2] < Int(krssi) {
                return 3.5 // Medium [2, 5]
            } else {
                return 8.0 // Far (more than 5)
            }
        } else {
            return nil;
        }
    }
    
    public override func reset() {
        median.reset()
    }
    
    public func medianOfRssi() -> Double? {
        return median.reduce()
    }
    
    public func kalmanOfRssi() -> Double? {
        return kalmanRssi
    }
}
