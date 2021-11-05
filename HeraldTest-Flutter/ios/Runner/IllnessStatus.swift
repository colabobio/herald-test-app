//
//  IllnessStatus.swift
//  Runner
//
//  Created by Andi Grozdani on 11/2/21.
//

//
//  IllnessStatus.swift
//  HeraldTest
//
//  Created by Andrés Colubri on 8/26/21.
//

import Herald

class IllnessStatus {
    var status: IllnessStatusCode = .susceptible
    var since: Date = Date()
    
    init(status: IllnessStatusCode, dateSince: Date) {
        self.status = status
        self.since = dateSince
    }
    
    func update(newStatus: IllnessStatusCode) {
        update(newStatus: newStatus, dateSince: Date())
    }
    
    func update(newStatus: IllnessStatusCode, dateSince: Date) {
        self.status = newStatus
        self.since = dateSince
    }
    
    func toPayload() -> PayloadData {
        let result = PayloadData()
        result.append(UInt64(self.since.timeIntervalSince1970))
        result.append(UInt8(self.status.rawValue))
        return result
    }
    
    func toString() -> String {
//        return "Status: \(status.rawValue), since (epoch): \(since.hashValue)"
        return "\(status.name)"
    }
    
    static func fromPayload(raw: Data) -> IllnessStatus {
        return IllnessStatus(status: IllnessStatusCode(rawValue: Int(raw.uint8(8)!))!, dateSince: Date(timeIntervalSince1970: Double(raw.uint64(0)!)))
    }
}

