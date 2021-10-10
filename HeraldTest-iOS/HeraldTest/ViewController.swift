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
            if let supplier = TestService.instance?.payloadDataSupplier {
                self.status.text = "\(supplier.getIdentifier()): \(supplier.getStatus())"
            }
        }
    }
        
    func updatePeers() {
        DispatchQueue.main.async {
            TestService.instance?.updateEditText(self.peers)
//            self.peers.text = ""
//
//            TestService.instance?.currentPeers.forEach({ (id: Int, value: PeerInfo) in
//                let txt = "-> \(id): \(value.status.toString()): RSSI=\(value.getRSSI()) \n"
//                print(txt)
//                self.peers.text.append(txt)
//            })
            
        }
    }
}
