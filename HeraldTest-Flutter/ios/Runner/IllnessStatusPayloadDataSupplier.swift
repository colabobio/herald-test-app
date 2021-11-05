//
//  IllnessStatusPayloadDataSupplier.swift
//  Runner
//
//  Created by Andi Grozdani on 11/2/21.
//

//
//  IllnessStatusPayloadDataSupplier.swift
//  HeraldTest
//
//

import Foundation
import Herald

class IllnessDataPayloadSupplier: PayloadDataSupplier {
    
    fileprivate var identifier:Int;
    fileprivate var status: IllnessStatus; // set by only constructor

    init(identifier: Int, status: IllnessStatus) {
        self.identifier = identifier
        self.status = status
    }

    init(identifier: Int) {
        self.identifier = identifier
        status = IllnessStatus(status: .susceptible, dateSince: Date())
    }

    func setStatus(newStatus: IllnessStatus) {
        self.status = newStatus
    }

    func getStatus() -> IllnessStatus {
        return status
    }

    func getIdentifier() -> Int{
        return identifier
    }

    func setIdentifier(identifier: Int) {
        self.identifier = identifier
    }

    func payload(_ timestamp: PayloadTimestamp, device: Device?) -> PayloadData? {
        // Return our status as a series of Bytes in a Data object instance
        // Note: Should cache this if it doesn't change often as a member in order to save processing CPU and thus battery
        let result = PayloadData();
        result.append(UInt64(identifier)); // bytes 0-7
        result.append(status.toPayload()); // bytes 8-16
        return result;
    }

    static func getIdentifierFromPayload(illnessPayload: PayloadData) -> Int {
        return Int(illnessPayload.data.uint64(0)!); // use first 8 bytes only
    }

    static func getIllnessStatusFromPayload(illnessPayload: PayloadData) -> IllnessStatus {
        return IllnessStatus.fromPayload(raw: illnessPayload.data.advanced(by: 8)); // don't include first 8 bytes
    }
    
}
