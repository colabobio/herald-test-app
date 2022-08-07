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
    private func createModel(_ UUID: Int) -> ModelWrapper {
        let model = ModelWrapper()
        self.modelMap[UUID] = model
        return model
    }

    // Adds a RSSI value to the model handling the given UUID (will create a new model if needed)
    public func addRSSI(_ UUID: Int, _ rssi: Double) {
        let sample = Sample(value: RSSI(rssi))
        let model = self.modelMap[UUID] ?? self.createModel(UUID)
        model.addSample(sample)
    }

    public func getDistance(_ UUID: Int) -> Double? { // TODO deal with not having enough samples to give distance or no model exists
        if let model = self.modelMap[UUID] {
            return model.getDistance()
        } else {
            return nil
        }
    }

    // This class essentially takes the role of SmoothedLinearModelAnalyzer and is heavily influenced by it
    // I made this class since SmoothedLinearModelAnalyzer is not *quite* what is required for this use
    private class ModelWrapper {

        var lastUpdated: Date
        var lastDistance: Double
        var model: SmoothedLinearModel

        var window: SampleList
        var maximumWindowSize: Int // TODO decide where these values go and how they work
        var smoothingWindow: TimeInterval


        init() {
            self.lastUpdated = Date()
            self.maximumWindowSize = 100
            self.smoothingWindow = TimeInterval(60)
            self.window = SampleList(maximumWindowSize)
            self.model = SmoothedLinearModel()
            
            self.lastDistance = 0
        }

        func addSample(_ sample: Sample) {
            window.push(sample: sample)
        }

        func getDistance() -> Double? { // TODO fix breaking for very close ranges
            let timeNow = Date()
            // Remove stale samples
            self.window.clearBeforeDate(Date(timeInterval: -self.smoothingWindow, since: timeNow))

            self.model.reset()
            for i in 0...self.window.size()-1 {
                if let sample = self.window.get(i) {
                    self.model.map(value: sample)
                }
            }
            return self.model.reduce()
        }
    }

}
