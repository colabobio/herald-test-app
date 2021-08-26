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
            let service = TestService.shared
            self.status.text = service.state
        }
    }
        
    func updatePeers() {
        DispatchQueue.main.async {
          // @Edison, here we should get the list of detected peers, now as peerStatus in the AppDelegate,
          // and add them one line at the time to the peers UITextView... forgot how to do that programatically :-)
        }
    }
}
