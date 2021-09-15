//
//  IllnessStatusCode.swift
//  HeraldTest
//
//

import Herald

class IllnessStatus {
    var status: IllnessStatusCode
    var date: Date
    
    init(status: IllnessStatusCode, dateSince: Date) {
        self.status = status
        self.date = dateSince
    }
    
    func update(newStatus: IllnessStatusCode) {
        update(newStatus: newStatus, dateSince: Date())
    }
    
    func update(newStatus: IllnessStatusCode, dateSince: Date) {
        self.status = newStatus
        self.date = dateSince
    }
    
    func toPayload() -> PayloadData {
        let result = PayloadData()
        result.append(UInt64(self.date.timeIntervalSince1970))
        result.append(UInt8(self.status.rawValue))
        return result
    }
    
    func toString() -> String {
        return "Status: \(status.rawValue), since (epoch): \(date.hashValue)"
    }
    
//    static func fromPayload(raw: Data) -> IllnessStatus {
//        return IllnessStatus(status: IllnessStatusCode(rawValue: Int(raw.uint8(8)!))!, dateSince: Date(from: Int(raw.uint64(0)!)))
//    }
    
    
    
    
}

