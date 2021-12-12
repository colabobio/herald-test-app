import UIKit
import Flutter

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
    
    fileprivate var _supplier: IllnessPayloadDataSupplier?
    fileprivate var _payloadData: [String : Any]?
    fileprivate var _uuid: Int = 0
    fileprivate var _illnessStatusCode: Int = 0
    fileprivate var _date: String = ""
    
    override func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        
        startTestService()
        
        let EVENT_CHANNEL_NAME = "com.herald_flutter.eventChannel"
        let METHOD_CHANNEL_NAME = "com.herald_flutter.methodChannel"
        let method = "initialPayload"
        
        let controller : FlutterViewController = window?.rootViewController as! FlutterViewController
        let methodChannel = FlutterMethodChannel(name: METHOD_CHANNEL_NAME, binaryMessenger: controller.binaryMessenger)
        let eventChannel = FlutterEventChannel(name: EVENT_CHANNEL_NAME,binaryMessenger: controller.binaryMessenger)
        
        methodChannel.setMethodCallHandler({
            (call: FlutterMethodCall, result: @escaping FlutterResult) -> Void in
            
            if (call.method == method) {
                
                guard let args = call.arguments else { return }
                
                self._payloadData = args as? [String: Any]
                self._uuid = (self._payloadData? ["uuid"] as? Int)!
                self._illnessStatusCode = (self._payloadData? ["illnessStatusCode"] as? Int)!
                self._date = (self._payloadData? ["date"] as? String)!
                
                var _date: Date
                let dateFormatterGet = DateFormatter()
                dateFormatterGet.dateFormat = "yyyy-MM-dd HH:mm:ss"
                _date = dateFormatterGet.date(from: self._date)!
                
                self._supplier = TestService.instance?.payloadDataSupplier
                self._supplier?.setIdentifier(identifier: self._uuid)
                self._supplier?.setIllnessStatusCode(illnessStatusCode: self._illnessStatusCode)
                self._supplier?.setDate(date: _date)
                
                return result(nil)
                
            }else {
                return result(FlutterMethodNotImplemented)
            }
        })
        
        eventChannel.setStreamHandler(TestService.instance)
        
        GeneratedPluginRegistrant.register(with: self)
        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }
    func startTestService() {
        TestService.shared.start()
    }
}
