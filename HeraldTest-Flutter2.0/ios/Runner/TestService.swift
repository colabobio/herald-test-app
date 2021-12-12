//
//  TestService.swift
//  Runner
//
//  Created by Andi Grozdani on 12/8/21.
//

import Foundation
import UIKit
import CoreBluetooth
import Flutter
import Herald

class TestService: NSObject, SensorDelegate, FlutterStreamHandler {
    
    static let DISABLE_HERALD: Bool = false
    static let SIMULATION_ID: String = "8693a908-43cf-44b3-9444-b91c04b83877"
    static let TIME_STEP: Int = 1
    
    var storePeersPayload: [String: Any] = [:]
    var payloadDataSupplier: IllnessPayloadDataSupplier?
    var sensor: SensorArray?
    
    public static var instance: TestService?
    
    var sink: FlutterEventSink?
    var payloadTimer: Timer?
    var timer: Timer?
    var dispatch: DispatchSourceTimer?
    var date: Date = Date()
    
    static var shared: TestService {
        if let service = TestService.instance { return service }
        let service = TestService()
        TestService.instance = service
        return service
    }
    
    func start() {
        payloadDataSupplier = IllnessPayloadDataSupplier(identifier: 1234567890, illnessStatusCode: 1, date: date)
        initSensor()
        startTimer()
    }
    
    private func startTimer() {
        let queue = DispatchQueue(label: "com.example.HeraldTest.timer", attributes: .concurrent)
        dispatch?.cancel()
        dispatch = DispatchSource.makeTimerSource(queue: queue)
        dispatch?.schedule(deadline: .now(), repeating: .seconds(TestService.TIME_STEP), leeway: .seconds(1))
        dispatch?.resume()
    }
    
    private func stopTimer() {
        dispatch?.cancel()
        dispatch = nil
        timer?.invalidate()
        timer = nil
    }
    
    func initSensor() {
        
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
        
        let statusPayload : StatusPayloadResults = payloadDataSupplier!.getIllnessStatusFromPayload(statusPayload: payloadData)
        
        let identifier : Int = payloadDataSupplier!.getIdentifierFromPayload(identifierPayload: payloadData)
        
        let illnessStatusCode: Int = statusPayload.getIllnessStatusCode()
        
        let date: Date = statusPayload.getDate()
        
        storePeersPayload.updateValue(identifier, forKey: "uuid")
        storePeersPayload.updateValue(illnessStatusCode, forKey: "code")
        storePeersPayload.updateValue(String(describing: date), forKey: "date")
        if (proximity?.value != nil) {
            storePeersPayload.updateValue(proximity?.value as Any, forKey: "rssi")
        }
        
        print("EVERYTHING", String(describing: storePeersPayload))
        
        print("RECEIVED PAYLOAD IDENTIFIER: ",  String(describing: identifier))
        print("RECEIVED ILLNESS STATUS CODE: ",  String(describing: illnessStatusCode))
        print("RECEIVED DATE: ",  String(describing: date))
        
    }
    
    @objc public func sendPeersPayload() {
        guard let sink = sink else { return }
        sink(storePeersPayload)
    }
    
    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        sink = events
        payloadTimer = Timer.scheduledTimer(timeInterval: 1.0, target: self, selector: #selector(sendPeersPayload), userInfo: nil, repeats: true)
        return nil
    }
    
    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        sink = nil
        payloadTimer?.invalidate()
        return nil
    }
}
