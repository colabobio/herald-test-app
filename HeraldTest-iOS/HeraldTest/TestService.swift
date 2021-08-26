//
//  TestService.swift
//  HeraldTest
//
//  Created by Andres Colubri on 8/26/21.
//

import Foundation

class TestService {
    let timeStep: Int = 2
    
    var timer: Timer?
    var dispatch: DispatchSourceTimer?
    
    private func startTimer() {
        let queue = DispatchQueue(label: "com.example.HeraldTest.timer", attributes: .concurrent)
        dispatch?.cancel() // cancel previous timer if any
        dispatch = DispatchSource.makeTimerSource(queue: queue)
        dispatch?.schedule(deadline: .now(), repeating: .seconds(timeStep), leeway: .seconds(1))
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
    
    private func update() {
    }
}
