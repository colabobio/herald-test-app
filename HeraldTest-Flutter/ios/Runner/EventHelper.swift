//
//  EventHelper.swift
//  Runner
//
//  Created by Andi Grozdani on 11/2/21.
//

//
//  EventHelper.swift
//  HeraldTest
//
//  Created by Andrés Colubri on 8/26/21.
//

import Foundation
import UIKit

protocol EventHelperDelegate: AnyObject {
    func updateStatus()->String
    func updatePeers()->String
}

enum EventHelper {
    weak static var delegate: EventHelperDelegate?
    
    static func triggerStatusChange() {
        delegate?.updateStatus()
    }
    
    static func triggerPeerDetect() {
        delegate?.updatePeers()
    }
}
