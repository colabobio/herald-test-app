//
//  TestService.swift
//  HeraldTest
//
//  Created by Andres Colubri on 8/26/21.
//

import Foundation
import UIKit

class TestService {
    static let TIME_STEP: Int = 2
    
    private static var instance: TestService?
    
    var id: String = ""
    var state: String = ""
    
    var time0: Int = 0
    
    var timer: Timer?
    var dispatch: DispatchSourceTimer?
    
    static var shared: TestService {
        if let service = TestService.instance { return service }
        let service = TestService()
        TestService.instance = service
        return service
    }
    
    func start() {
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
        // @Edison: need to update Herald payload here...
        
        // and broacast it, I guess with
        
        // sensor!.immediateSendAll(data: d)
        
        // but sensor is defined in the AppDelegate
    }
    
    private func updateLoop() {
        updateState()
        
        print("in update loop")
    }
}
