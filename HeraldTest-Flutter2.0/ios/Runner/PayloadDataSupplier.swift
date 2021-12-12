//
//  PayloadDataSupplier.swift
//  Runner
//
//  Created by Andi Grozdani on 12/8/21.
//

import Foundation
import Herald

class IllnessPayloadDataSupplier: PayloadDataSupplier {
    
    fileprivate var identifier: Int
    fileprivate var illnessStatusCode: Int
    fileprivate var date : Date
    
    init(identifier: Int, illnessStatusCode: Int, date: Date) {
        self.identifier = identifier
        self.illnessStatusCode = illnessStatusCode
        self.date = date
    }
    
    func setIdentifier(identifier: Int) {
        self.identifier = identifier
    }
    
    func setIllnessStatusCode(illnessStatusCode: Int) {
        self.illnessStatusCode = illnessStatusCode
    }
    
    func setDate(date: Date) {
        self.date = date
    }
    
    func getIdentifier() -> Int{
        return identifier
    }
    
    func getIllnessStatusCode() -> Int{
        return illnessStatusCode
    }
    
    func getDate() -> Date{
        return date
    }
    
    func payload(_ timestamp: PayloadTimestamp, device: Device?) -> PayloadData? {
        // Return our status as a series of Bytes in a Data object instance
        // Note: Should cache this if it doesn't change often as a member in order to save processing CPU and thus battery
        let result = PayloadData();
        result.append(UInt64(identifier)); // bytes 0-7
        result.append(toPayload()); // bytes 8-16
        return result;
    }
    
    func getIdentifierFromPayload(identifierPayload: PayloadData) -> Int {
        return Int(identifierPayload.data.uint64(0)!); // use first 8 bytes only
    }
    
    func getIllnessStatusFromPayload(statusPayload: PayloadData) -> StatusPayloadResults {
        return fromPayload(raw: statusPayload.data.advanced(by: 8)); // don't include first 8 bytes
    }
    
    func toPayload() -> PayloadData {
        let result = PayloadData()
        result.append(UInt64(self.date.timeIntervalSince1970))
        result.append(UInt8(self.illnessStatusCode))
        return result
    }
    
    func fromPayload(raw: Data) -> StatusPayloadResults {
        
        let date : Date = Date(timeIntervalSince1970: Double(raw.uint64(0)!))
        let illnessStatusCode : Int = Int(raw.uint8(8)!)
        
        return StatusPayloadResults(date: date, illnessStatusCode: illnessStatusCode)
    }
}

class StatusPayloadResults {
    fileprivate var date : Date
    fileprivate var illnessStatusCode : Int
    
    init(date: Date, illnessStatusCode: Int) {
        self.date = date
        self.illnessStatusCode = illnessStatusCode
    }
    
    func getDate() -> Date {
        return date
    }
    
    func getIllnessStatusCode() -> Int {
        return illnessStatusCode
    }
    
}
