import Herald
import Darwin

// Note: "model" is used both for SmoothedLinerModel and ModelWrapper.

public class DistanceEstimator {
    private var modelMap: [Int: ModelWrapper] = [:]
    private var tag: String = "DistanceEstimator"

    public init() {

    }

    // Removes the model associated with the given UUID
    public func removeModel(_ UUID: Int) {
        print("\(tag) Removed UUID: \(UUID)")
        self.modelMap.removeValue(forKey: UUID)
    }

    // Creates a model for a given UUID
    private func createModel(_ UUID: Int, _ phone: Int) -> ModelWrapper {
        let model = ModelWrapper(phoneCode: phone)
        self.modelMap[UUID] = model
        return model
    }

    // Adds a RSSI value to the model handling the given UUID (will create a new model if needed)
    public func addRSSI(_ UUID: Int, _ rssi: Double, _ phone: Int) {
        let sample = Sample(value: RSSI(rssi))
        let model = self.modelMap[UUID] ?? self.createModel(UUID, phone)
        model.addSample(sample)
    }

    public func getDistance(_ UUID: Int) -> Double? { // TODO deal with not having enough samples to give distance or no model exists
        if let model = self.modelMap[UUID] {
            return model.getDistance()
        } else {
            return nil
        }
    }

    public func getSampleCount(_ UUID: Int) -> Int {
        if let model = self.modelMap[UUID] {
            return model.getSampleCount()
        } else {
            return 0
        }
    }
    
    public func getMedianRSSI(_ UUID: Int) -> Double? {
        if let model = self.modelMap[UUID] {
            return model.getMedianRSSI()
        } else {
            return nil
        }
    }

    // This class essentially takes the role of SmoothedLinearModelAnalyzer and is heavily influenced by it
    // I made this class since SmoothedLinearModelAnalyzer is not *quite* what is required for this use
    private class ModelWrapper {

        var lastUpdated: Date
        var lastDistance: Double
        var model: CoarseDistanceModel
//        var filter: SimpleKalmanFilter

        var window: SampleList
        var minimumWindowSize: Int
        var maximumWindowSize: Int // TODO decide where these values go and how they work
        var smoothingWindow: TimeInterval


        init(phoneCode: Int) {
            self.lastUpdated = Date()
            self.minimumWindowSize = 10
            self.maximumWindowSize = 50
            self.smoothingWindow = TimeInterval(60)
            self.window = SampleList(maximumWindowSize)
            self.model = CoarseDistanceModel(phoneCode: phoneCode)
//            self.filter = SimpleKalmanFilter(1, 1, 0.03)
            
            self.lastDistance = 0
        }

        func addSample(_ sample: Sample) {
            window.push(sample: sample)
        }

        func getSampleCount() -> Int {
            return window.size()
        }
        
        func getMedianRSSI() -> Double? {
            return self.model.medianOfRssi()
        }

        func getDistance() -> Double? { // TODO fix breaking for very close ranges
            let timeNow = Date()
            // Remove stale samples
            self.window.clearBeforeDate(Date(timeInterval: -self.smoothingWindow, since: timeNow))

            if self.window.size() < minimumWindowSize {
               return nil
            }
            
            self.model.reset()
            for i in 0...self.window.size()-1 {
                if let sample = self.window.get(i) {
                    self.model.map(value: sample)
                }
            }
            
            let distanceBucket = self.model.reduce()
//            let distanceEstimation =  self.filter.updateEstimate(distanceMeasurement)
            return distanceBucket
        }
    }

}
