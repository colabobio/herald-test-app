//
//  IllnessStatusCode.swift
//  HeraldTest
//
//

enum IllnessStatusCode : Int, CaseIterable {
    case susceptible = 1
    case infected = 2
    case transmittable = 3
    case illAndTransmittable = 4
    case illAndNonTransmittable = 5
    case recovered = 6
    case vaccinated = 7
    case immune = 8
    case nonTransmittable = 9
    
    var name: String {
        switch self {
        case .susceptible: return "susceptible"
        case .infected: return "infected"
        case .transmittable: return "transmittable"
        case .illAndTransmittable: return "illAndTransmittable"
        case .illAndNonTransmittable: return "illAndNonTransmittable"
        case .recovered: return "recovered"
        case .vaccinated: return "vaccinated"
        case .immune: return "immune"
        case .nonTransmittable: return "nonTransmittable"
        }
    }
    
    class IllnessStatusCode {
        
        private var value:Int
        
        private init(_ val: Int) {
            self.value = val
        }
    }
}


