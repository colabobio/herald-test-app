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
    
    init() {
        initState()
        startTimer()
    }
    
    private func startTimer() {
        let queue = DispatchQueue(label: "com.example.HeraldTest.timer", attributes: .concurrent)
        dispatch?.cancel() // cancel previous timer if any
        dispatch = DispatchSource.makeTimerSource(queue: queue)
        dispatch?.schedule(deadline: .now(), repeating: .seconds(TestService.TIME_STEP), leeway: .seconds(1))
        dispatch?.setEventHandler { [weak self] in // `[weak self]` only needed if you reference `self` in this closure and you want to prevent strong reference cycle
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
        id = UUID().uuidString
        time0 = Int(Date().timeIntervalSince1970)
        state = id + ":0"
    }
    
    private func updateState() {
        state = id + ": \((Int(Date().timeIntervalSince1970) - time0))"
        updatePayload()
    }
    
    private func updatePayload() {
        // @Edison: need to update Herald payload here.
    }
    
    private func updateLoop() {
        updateState()
        
        print("in update loop", state)
    }
}
