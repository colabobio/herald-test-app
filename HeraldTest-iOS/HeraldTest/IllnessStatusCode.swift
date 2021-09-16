//
//  IllnessStatusCode.swift
//  HeraldTest
//
//

enum IllnessStatusCode : Int, CaseIterable {
    case susceptable = 1
    case infected = 2
    case transmittable = 3
    case illAndTransmittable = 4
    case illAndNonTransmittable = 5
    case recovered = 6
    case vaccinated = 7
    case immune = 8
    case nonTransmittable = 9
    
    class IllnessStatusCode {
        
        private var value:Int
        
        private init(_ val: Int) {
            self.value = val
        }
    }
}


