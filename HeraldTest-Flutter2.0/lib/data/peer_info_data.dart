import 'package:intl/intl.dart';

class PeerInfo {
  late List<double> _data;
  late int _illnessStatusCode;
  late DateTime _lastseen;
  /* update count is for keeping track of how many
  times the illness status code changed in the current peer */
  late int _updateCount;

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

  void addRSSI(double value) {
    if (-100 <= value && value < 0) {
      _data.add(value);
      if (25 < _data.length) {
        _data.removeAt(0);
      }
    }
  }

  double getRSSI() {
    if (_data.isNotEmpty) {
      double sum = 0;
      for (double v in _data) {
        sum += v;
      }
      return sum / _data.length;
    } else {
      return -50;
    }
  }
}
