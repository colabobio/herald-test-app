import 'package:intl/intl.dart';

class PeerInfo {
  late List<double> _data;
  late int _illnessStatusCode;
  late DateTime _lastseen;
  /* update count is for keeping track of how many
  times the illness status code changed in the current peer */
  late int _updateCount;
  late double _distance;

  PeerInfo() {
    _data = List<double>.empty(growable: true);
    _illnessStatusCode = 0;
    _lastseen = DateFormat('yyyy-MM-dd HH:mm:ss')
        .parse(DateTime.now().toUtc().toString());
    _updateCount = 0;
  }

  void setStatus(int illnessStatusCode) {
    _illnessStatusCode = illnessStatusCode;
    _lastseen = DateFormat('yyyy-MM-dd HH:mm:ss')
        .parse(DateTime.now().toUtc().toString());
    _updateCount += 1;
  }

  List<double> getData() {
    return _data;
  }

  int getIllnessStatus() {
    return _illnessStatusCode;
  }

  DateTime getLastSeen() {
    return _lastseen;
  }

  String getUpdateCount() {
    return _updateCount.toString();
  }

  void setRSSI(double rssi) {
    if (-100 <= rssi && rssi < 0) {
      _data.add(rssi);
      if (25 < _data.length) {
        _data.removeAt(0);
      }
    }
    _lastseen = DateFormat('yyyy-MM-dd HH:mm:ss')
        .parse(DateTime.now().toUtc().toString());
  }

  double getRSSI() {
    return _data.last;
  }

  void setDistance(double distance) {
    _distance = distance;
  }

  double getDistance() {
    return _distance;
  }
}
