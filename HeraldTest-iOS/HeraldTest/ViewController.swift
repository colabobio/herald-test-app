//
//  ViewController.swift
//  HeraldTest
//
//  Created by Andres Colubri on 8/25/21.
//

import UIKit

class ViewController: UIViewController {

    @IBOutlet weak var status: UILabel!    
    @IBOutlet weak var peers: UITextView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        TestService.shared.start()
        
        EventHelper.delegate = self
    }

}

extension ViewController: EventHelperDelegate {
    
    func updateStatus() {
        DispatchQueue.main.async {
            if let supplier = AppDelegate.instance?.payloadDataSupplier {
                self.status.text = "\(supplier.getIdentifier()): \(supplier.getStatus())"
            }
        }
    }
        
    func updatePeers() {
        DispatchQueue.main.async {
            self.peers.text = ""
                AppDelegate.instance?.currentPeers.forEach({ (id: Int, value: PeerInfo) in
                
                let lastFiveMinutes = Date().timeIntervalSince(value.lastSeen)
                let fiveMinutesFromNow: TimeInterval = 60 * 5
                
                if (lastFiveMinutes >= fiveMinutesFromNow) {
                    AppDelegate.instance?.currentPeers.removeValue(forKey: id)
                }
                
                let txt = "-> \(id): \(value.status): RSSI=\(value.getRSSI()) \n"
                print(txt)
                self.peers.text.append(txt)
            })
        }
    }
}
