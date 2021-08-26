//
//  TestService.swift
//  HeraldTest
//
//  Created by Andres Colubri on 8/26/21.
//

import Foundation

class TestService {
    static let TIME_STEP: Int = 2
    
    private static var instance: TestService?
    
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
            self?.update()
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
        
    }
    
    private func update() {
        print("update")
    }
}
