import Herald

// Note: "model" is used both for SmoothedLinerModel and ModelWrapper.

public class TestService: NSObject {
    var modelMap: [Int: ModelWrapper] = [:]
    static let tag: String = "DistanceEstimator"

    public init() {

    }

    //removes the model associated with the given UUID
    public func removeModel(_ UUID: Int) {
        print(tag + " Removed UUID: " + UUID)
        self.modelMap.remove(UUID)
    }

    //creates a model for a given UUID
    private func createModel(_ UUID: Int) -> ModelWrapper {
        let model = ModelWrapper()
        self.modelMap.put(UUID, model)
        return model
    }

    //adds a RSSI value to the model handling the given UUID (will create a new model if needed)
    public func addRSSI(_ UUID: Int, _ rssi: Double) {
        let sample = Sample(RSSI(rssi))
        if let model = modelMap.get(UUID) {
            model.addSample(sample)
        } else if let model = createModel(UUID) {
            model.addSample(sample)        
        }
    }

    public func getDistance(_ UUID: Int) -> Double { // TODO deal with not having enough samples to give distance or no model exists
        let model = modelMap.get(UUID)
        return model.getDistance()
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
            self.smoothingWindow = TimeInterval(10)
            self.window = SampleList(maximumWindowSize)
            self.model = SmoothedLinearModel()
        }

        func addSample(_ sample: Sample){
            window.push(sample)
        }

        func getDistance() -> Double { // TODO fix breaking for very close ranges
            Date timeNow = Date();
            //remove stale samples
            window.clearBeforeDate(Date(timeNow.secondsSinceUnixEpoch() - smoothingWindow.value))

            model.reset()
            for sample in window {
                model.map(sample)
            }
            return model.reduce()
        }
    }

}