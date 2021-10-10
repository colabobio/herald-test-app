//
//  TestService.swift
//  HeraldTest
//
//  Created by Andres Colubri on 8/26/21.
//

import Foundation
import UIKit
import Herald

class TestService: SensorDelegate {
    static let TIME_STEP: Int = 2
    
    var currentPeers: [Int: PeerInfo] = [:]
    var payloadDataSupplier: IllnessDataPayloadSupplier?
    var sensor: SensorArray?
    
    let RSSI_THRESHOLD = -30.0
    
    public static var instance: TestService?
    
    var id: String = ""
    var state: String = ""
    
    var time0: Int = 0
    
    var timer: Timer?
    var dispatch: DispatchSourceTimer?
    
    private var uniqueID: String = ""
    static let PREF_UNIQUE_ID: String = "PREF_UNIQUE_DEVICE_ID"
    private func getUniqueId() -> String {
        if uniqueID == "" {
            uniqueID = UserDefaults.standard.string(forKey: TestService.PREF_UNIQUE_ID) ?? ""
            if uniqueID == "" {
                uniqueID = UUID().uuidString
                UserDefaults.standard.set(uniqueID, forKey: TestService.PREF_UNIQUE_ID)
            }
        }
        return uniqueID
    }
    
    private func hashCode(_ text: String) -> Int {
        var hash = UInt64 (5381)
        let buf = [UInt8](text.utf8)
        for b in buf {
            hash = 127 * (hash & 0x00ffffffffffffff) + UInt64(b)
        }
        let value = Int(hash.remainderReportingOverflow(dividingBy: UInt64(Int32.max)).partialValue)
        return value
    }
    
    private func identifier() -> Int {
//           let text = UIDevice.current.name + ":" + UIDevice.current.model + ":" + UIDevice.current.systemName + ":" + UIDevice.current.systemVersion
//           var hash = UInt64 (5381)
//           let buf = [UInt8](text.utf8)
//           for b in buf {
//               hash = 127 * (hash & 0x00ffffffffffffff) + UInt64(b)
//           }
//           let value = Int32(hash.remainderReportingOverflow(dividingBy: UInt64(Int32.max)).partialValue)
         let v = getUniqueId()
         let h = hashCode(v)
         return h
     }

    func startPhone() {
        payloadDataSupplier = IllnessDataPayloadSupplier(identifier: identifier())
        BLESensorConfiguration.payloadDataUpdateTimeInterval = TimeInterval.minute
        BLESensorConfiguration.logLevel = .debug
        sensor = SensorArray(payloadDataSupplier!)
        sensor?.add(delegate: self)
        sensor?.start()
    }
    
    static var shared: TestService {
        if let service = TestService.instance { return service }
        let service = TestService()
        TestService.instance = service
        return service
    }
    
    func start() {
        startPhone()
        initState()
        startTimer()
    }
    
    private func startTimer() {
        let queue = DispatchQueue(label: "com.example.HeraldTest.timer", attributes: .concurrent)
        dispatch?.cancel()
        dispatch = DispatchSource.makeTimerSource(queue: queue)
        
        dispatch?.schedule(deadline: .now(), repeating: .seconds(TestService.TIME_STEP), leeway: .seconds(1))
        dispatch?.setEventHandler { [weak self] in
            self?.updateLoop()
        }
        dispatch?.resume()
    }
    
    private func stopTimer() {
        dispatch?.cancel()
        dispatch = nil
        timer?.invalidate()
        timer = nil
    }
    
    private func initState() {
        id = randomID()
        time0 = Int(Date().timeIntervalSince1970)
        state = id + ":0"
    }
    
    private func updateState() {
        state = id + ": \((Int(Date().timeIntervalSince1970) - time0))"
        
        updatePayload()
        
        EventHelper.triggerStatusChange()
    }
    
    func randomID() -> String {
      let letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
      let digits = "0123456789"
      return String((0..<2).map{ _ in letters.randomElement()! }) + String((0..<2).map{ _ in digits.randomElement()! })
    }
    
    private func updatePayload() {
//        AppDelegate.instance?.payloadDataSupplier?.payload(Data(state.utf8))
        payloadDataSupplier?.setStatus(newStatus: IllnessStatus(status: IllnessStatusCode.allCases.randomElement() ?? .susceptable, dateSince: Date()))
    }
    
    private func updateLoop() {
        updateState()
        
        print("in update loop")
    }
    
    // MARK:- SensorDelegate
    
    func sensor(_ sensor: SensorType, didDetect: TargetIdentifier) {
        print(sensor.rawValue + ",didDetect=" + didDetect.description)
    }
    
    // TODO: Get Message
    func sensor(_ sensor: SensorType, didRead: PayloadData, fromTarget: TargetIdentifier) {
        print(sensor.rawValue + ",didRead=" + didRead.shortName + ",fromTarget=" + fromTarget.description)
        parsePayload("didRead", sensor, didRead, nil, fromTarget)
    }
    
    // Get Message
    func sensor(_ sensor: SensorType, didReceive: Data, fromTarget: TargetIdentifier) {
        print(sensor.rawValue + ",didReceive=" + didReceive.base64EncodedString() + ",fromTarget=" + fromTarget.description)
    }
    
    // TODO: Gets us proximity
    func sensor(_ sensor: SensorType, didMeasure: Proximity, fromTarget: TargetIdentifier) {
        print(sensor.rawValue + ",didMeasure=" + didMeasure.description + ",fromTarget=" + fromTarget.description)
    }
    
    func sensor(_ sensor: SensorType, didVisit: Location?) {
        print(sensor.rawValue + ",didVisit=" + String(describing: didVisit))
    }
    
    func sensor(_ sensor: SensorType, didMeasure: Proximity, fromTarget: TargetIdentifier, withPayload: PayloadData) {
        print(sensor.rawValue + ",didMeasure=" + didMeasure.description + ",fromTarget=" + fromTarget.description + ",withPayload=" + withPayload.shortName)
        
        parsePayload("didMeasure", sensor, withPayload, didMeasure, fromTarget)
    }
    
    func sensor(_ sensor: SensorType, didUpdateState: SensorState) {
        print(sensor.rawValue + ",didUpdateState=" + didUpdateState.rawValue)
    }
    
    func parsePayload(_ source: String, _ sensor: SensorType, _ payloadData: PayloadData, _ proximity: Proximity?, _ fromTarget: TargetIdentifier) {
        
        let identifer = IllnessDataPayloadSupplier.getIdentifierFromPayload(illnessPayload: payloadData)
        let status = IllnessDataPayloadSupplier.getIllnessStatusFromPayload(illnessPayload: payloadData)
    
        print("RECEIVED PAYLOAD IDENTIFIER: ", identifer)
        print("RECEIVED STATUS: ", status.toString())
        
        var info = currentPeers[identifer]
        
        if (info == nil) {
            info = PeerInfo()
            info!.status = status
            currentPeers[identifer] = info
        }
       
        if (proximity != nil) {
            info!.addRSSI(value: proximity!.value)
            print("RSSI value: ", proximity!.value)
        }
        
        
        if (RSSI_THRESHOLD < info!.getRSSI() && 10 < info!.data.count) {
            // not in contact anymore, remove
            currentPeers.removeValue(forKey: identifer)
        }
        
        EventHelper.triggerPeerDetect()
        
    }

}
