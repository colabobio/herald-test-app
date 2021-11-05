//
//  PeerInfo.swift
//  Runner
//
//  Created by Andi Grozdani on 11/2/21.
//

import Foundation

class PeerInfo {
    public var data: [Double] = []
    public var status: IllnessStatus
    public var lastSeen: Date
    public var updateCount: Int
    
    init(data: [Double], status: IllnessStatus, lastSeen: Date) {
        self.data = data
        self.status = status
        self.lastSeen = lastSeen
        self.updateCount = 0
    }
    
    init() {
        self.data = []
        self.status = IllnessStatus.init(status: .susceptible, dateSince: Date())
        self.lastSeen = Date()
        self.updateCount = 0
    }
    
    func setStatus(_ status: IllnessStatus) {
        self.status = status
        self.lastSeen = Date()
        self.updateCount += 1
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
            return -50;
        }
    }
}
