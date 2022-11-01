import 'package:herald_flutter/widgets/generate_date.dart';
import 'package:intl/intl.dart';

class PeerInfo {
  late List<double> _data;
  late int _illnessStatusCode;
  late DateTime _lastseen;
  /* update count is for keeping track of how many
  times the illness status code changed in the current peer */
  late int _updateCount;
  late int _sampleCount;
  late double _distance;
  late int _phoneCode;

  final GenerateDate _generateDate = GenerateDate();

  PeerInfo() {
    _data = List<double>.empty(growable: true);
    _illnessStatusCode = 0;
    _lastseen = DateFormat('yyyy-MM-dd HH:mm:ss')
        .parse(DateTime.now().toUtc().toString());
    _updateCount = 0;
    _sampleCount = 0;
    _distance = -1;
    _phoneCode = 0;
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

  int secondsSinceLastSeen() {
    DateTime lastSeen = _lastseen;
    DateTime newDateTime = DateFormat('yyyy-MM-dd HH:mm:ss')
        .parse(DateTime.now().toUtc().toString());
    //difference between dates is in seconds
    int difference =
        _generateDate.differenceBetweenDates(lastSeen, newDateTime);
    return difference;
  }

  String getUpdateCount() {
    return _updateCount.toString();
  }

  void setSampleCount(int count) {
    _sampleCount = count;
  }

  String getSampleCount() {
    return _sampleCount.toString();
  }

  void setPhoneCode(int phone) {
    _phoneCode = phone;
  }

  String getPhoneCode() {
    return _phoneCode.toString();
  }

  void setRSSI(double rssi) {
    if (-100 <= rssi && rssi < 0) {
      _data.add(rssi);
      if (100 < _data.length) {
        _data.removeAt(0);
      }
    }
    _lastseen = DateFormat('yyyy-MM-dd HH:mm:ss')
        .parse(DateTime.now().toUtc().toString());
  }

  double getRSSI() {
    if (_data.isEmpty) return 0;
    if (_data.length == 1) return _data[0];

    //clone list
    List<double> clonedList = List<double>.empty(growable: true);
    clonedList.addAll(_data);

    //sort list
    clonedList.sort((a, b) => a.compareTo(b));

    double median;
    int middle = clonedList.length ~/ 2;
    if (clonedList.length % 2 == 1) {
      median = clonedList[middle];
    } else {
      median = (clonedList[middle - 1] + clonedList[middle]) / 2.0;
    }

    return median;
  }

  void setDistance(double distance) {
    _distance = distance;
  }

  double getDistance() {
    return _distance;
  }
}
