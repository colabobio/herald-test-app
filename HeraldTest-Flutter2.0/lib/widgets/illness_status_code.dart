import 'dart:math';

enum IllnessStatus {
  susceptible,
  infected,
  transmittable,
  illAndTransmittable,
  illAndNonTransmittable,
  recovered,
  vaccinated,
  immune,
  nonTransmittable
}

class IllnessStatusCode {
  IllnessStatus getRandomStatus() {
    List<IllnessStatus> values = IllnessStatus.values;
    int length = values.length;
    int randIndex = Random().nextInt(length);
    return values[randIndex];
  }
}
