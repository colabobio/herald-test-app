//
//  AppDelegate.swift
//  Runner
//
//  Created by Andi Grozdani on 11/2/21.
//

import UIKit
import Flutter

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
    override func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        
        startTestService()
        
        let METHOD_CHANNEL_NAME = "com.flutterxherald.methodChannel"
        
        let controller : FlutterViewController = window?.rootViewController as! FlutterViewController
        let methodChannel = FlutterMethodChannel(name: METHOD_CHANNEL_NAME, binaryMessenger: controller.binaryMessenger)
        
        methodChannel.setMethodCallHandler({
            (call: FlutterMethodCall, result: @escaping FlutterResult) -> Void in
          
            if (call.method == "isStatusAvailable") {
                let statusResult = self.updateStatus()
                return result(statusResult)
            }
            if (call.method == "isPeersAvailable") {
                let peersResult = self.updatePeers()
                return result(peersResult)
            }
                return result(FlutterMethodNotImplemented)
        })
        
        GeneratedPluginRegistrant.register(with: self)
        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }
    func startTestService() {
        EventHelper.delegate = self
        TestService.shared.start()
    }
}


extension AppDelegate: EventHelperDelegate {
    func updateStatus() -> (String) {
        if (TestService.instance != nil) {
            let supplier = TestService.instance?.payloadDataSupplier
            
            return "\(supplier!.getIdentifier()): \(supplier!.getStatus().toString())"
        }
        return "not working"
    }
    
    func updatePeers() -> String {
        if (TestService.instance?.updateEditText() != nil) {
            return (TestService.instance?.updateEditText())!
        }
        return "not working"
    }
}
