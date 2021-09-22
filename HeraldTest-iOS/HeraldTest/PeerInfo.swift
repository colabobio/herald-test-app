//
//  PeerInfo.swift
//  HeraldTest
//
//

import Foundation

class PeerInfo {
    public var data: [Double] = []
    public var status: IllnessStatus
    
    init(data: [Double], status: IllnessStatus) {
        self.data = data
        self.status = status
    }
    
    init() {
        self.data = []
        self.status = IllnessStatus.init(status: .susceptable, dateSince: Date())
    }
    
    func addRSSI(value: Double) {
        if (-100 <= value && value < 0) {
            data.append(value);
            if (25 < data.count) {
               data.remove(at:0);
            }
        }
    }
    
    func getRSSI() -> Double {
        if (0 < data.count) {
            var sum = 0.0;
            for v in data {
                sum += v
            }
            return sum / Double(data.count);
        } else {
            return -100;
        }
    }
}