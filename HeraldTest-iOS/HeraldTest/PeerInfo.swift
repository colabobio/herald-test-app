//
//  PeerInfo.swift
//  HeraldTest
//
//

import Foundation

class PeerInfo {
    public var data: [Double] = []
    public var status: IllnessStatus
    public var lastSeen: Date
    
    init(data: [Double], status: IllnessStatus, lastSeen: Date) {
        self.data = data
        self.status = status
        self.lastSeen = lastSeen
    }
    
    init() {
        self.data = []
        self.status = IllnessStatus.init(status: .susceptible, dateSince: Date())
        self.lastSeen = Date()
    }
    
    func setStatus(_ status: IllnessStatus) {
        self.status = status
        self.lastSeen = Date()
    }
    
    func addRSSI(_ value: Double) {
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
