//
//  TestService.swift
//  HeraldTest
//
//  Created by Andres Colubri on 8/26/21.
//

import Foundation
import UIKit
import CoreBluetooth
import Herald

class TestService: SensorDelegate {
    static let DISABLE_HERALD: Bool = false
    static let SIMULATION_ID: String = "8693a908-43cf-44b3-9444-b91c04b83877"
    
    static let TIME_STEP: Int = 2
    static let REMOVE_TIME: Double = 30
    static let UPDATE_TIME: Double = 2
    
    var currentPeers: [Int: PeerInfo] = [:]
    var payloadDataSupplier: IllnessDataPayloadSupplier?
    var sensor: SensorArray?
    
    let RSSI_THRESHOLD = -70.0
    let MIN_RSSI_VALUES = 10
    
    public static var instance: TestService?
    
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
         let id = getUniqueId()
         return hashCode(id)
     }
    
    static var shared: TestService {
        if let service = TestService.instance { return service }
        let service = TestService()
        TestService.instance = service
        return service
    }
    
    func start() {
        initSensor()
        startTimer()
        selectRandomState()
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
    
    func initSensor() {
        payloadDataSupplier = IllnessDataPayloadSupplier(identifier: identifier())
        
        if (TestService.DISABLE_HERALD) {
            return
        }
        
        BLESensorConfiguration.payloadDataUpdateTimeInterval = TimeInterval.minute
        
        // This allow us to have multiple teams playing in the same area and not interfering each other
        // https://www.uuidgenerator.net/version4
        BLESensorConfiguration.serviceUUID = CBUUID(string: TestService.SIMULATION_ID)
        
        BLESensorConfiguration.logLevel = .debug
        sensor = SensorArray(payloadDataSupplier!)
        sensor?.add(delegate: self)
        sensor?.start()
    }
    
    private func updateState() {
        updatePayload()
    }
    
    private func updatePayload() {
        let lastUpdate = Date().timeIntervalSince((payloadDataSupplier?.getStatus().since)!)
        let someMinutesFromNow: TimeInterval = 60 * TestService.UPDATE_TIME
        if (lastUpdate >= someMinutesFromNow) {
            selectRandomState()
        }
    }
    
    private func selectRandomState() {
        payloadDataSupplier?.setStatus(newStatus: IllnessStatus(status: IllnessStatusCode.allCases.randomElement() ?? .susceptible, dateSince: Date()))
        EventHelper.triggerStatusChange()
    }
    
    private func removeLostPeers() {
        var removed = false
        currentPeers.forEach({ (id: Int, value: PeerInfo) in
            
            let lastSeen = Date().timeIntervalSince(value.lastSeen)
            let someMinutesFromNow: TimeInterval = 60 * TestService.REMOVE_TIME
            
            if (lastSeen >= someMinutesFromNow) {
                removed = true
                TestService.instance?.currentPeers.removeValue(forKey: id)
                print("Removed lost peer \(id)")
            }
        })
        
        if (removed) {
            EventHelper.triggerPeerDetect()
        }
    }
    
    private func updateLoop() {
        updateState()
        removeLostPeers()
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
    
    // Handles "nearby characteristic" feature: Read payload data of other targets recently acquired by a target, e.g.
    // Android peripheral sharing payload data acquired from nearby iOS peripherals.
    func sensor(_ sensor: SensorType, didShare: [PayloadData], fromTarget: TargetIdentifier) {
        var str: String = ""
        for payload in didShare {
            str += ":" + payload    .shortName
        }
        print(sensor.rawValue + ",didShare=" + str + ",fromTarget=" + fromTarget.description)
        for payload in didShare {
            parsePayload("didShare", sensor, payload, nil, fromTarget)
        }
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
            currentPeers[identifer] = info
        }
        info?.setStatus(status)
       
        if (proximity != nil) {
            info?.addRSSI(proximity!.value)
            print("RSSI value: ", proximity!.value)
        }
        
        
        if (MIN_RSSI_VALUES <= info!.data.count && info!.getRSSI() < RSSI_THRESHOLD) {
            // not in contact anymore, remove
            currentPeers.removeValue(forKey: identifer)
        }
        
        EventHelper.triggerPeerDetect()
    }
    
    func updateEditText(_ peers: UITextView) {
        peers.text = ""
        currentPeers.forEach({ (id: Int, value: PeerInfo) in
            if (MIN_RSSI_VALUES <= value.data.count) {
                let txt = "-> \(id): \(value.status.toString()): RSSI=\(value.getRSSI()): UPC=\(value.updateCount)\n"
                print(txt)
                peers.text.append(txt)
            }
        })
    }

}
