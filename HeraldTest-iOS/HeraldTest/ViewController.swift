//
//  ViewController.swift
//  HeraldTest
//
//  Created by Andres Colubri on 8/25/21.
//

import UIKit

class ViewController: UIViewController {

    @IBOutlet weak var status: UILabel!
    
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
    
}
