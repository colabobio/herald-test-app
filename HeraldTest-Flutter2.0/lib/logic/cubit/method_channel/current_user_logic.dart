import 'package:flutter/services.dart';
import 'package:intl/intl.dart';

import '../../../core/constants/strings.dart';
import '../../../data/illness_status_code_data.dart';
import '../../../data/shared_prefs/shared_prefs_data.dart';
import '../../generate_date.dart';

IllnessStatusCode _illnessStatusCode = IllnessStatusCode();
GenerateDate _generateDate = GenerateDate();

class CurrentUserLogic {
  final MethodChannel _methodChannel =
      const MethodChannel(Strings.methodChannelName);

  String _uuid = '';
  IllnessStatus _randomIllnessStatusCode = _illnessStatusCode.getRandomStatus();
  String _date = _generateDate.generateDate();

  Future<void> sendData() async {
    int uuid = SharedPrefsData().getIdentifier;
    _uuid = uuid.toString();
    try {
      await _methodChannel.invokeMethod(
          Strings.initalPayload, <String, dynamic>{
        'uuid': uuid,
        'illnessStatusCode': _randomIllnessStatusCode.index,
        'date': _date
      });
    } on PlatformException catch (e) {
      // ignore: avoid_print
      print(e.message);
    }
    SharedPrefsData().setIdentifier(uuid);
  }

  bool updateIllnessStatusCode() {
    bool beenTwoMinutes = false;
    String newDate = _generateDate.generateDate();

    int difference = _generateDate.differenceBetweenDates(
        DateFormat('yyyy-MM-dd HH:mm:ss').parse(_date),
        DateFormat('yyyy-MM-dd HH:mm:ss').parse(newDate));

    if (difference >= 120) {
      _date = newDate;
      _randomIllnessStatusCode = _illnessStatusCode.getRandomStatus();
      beenTwoMinutes = true;
    }
    return beenTwoMinutes;
  }

  String getCurrentUserInfoText() {
    return '$_uuid $_randomIllnessStatusCode (' +
        _randomIllnessStatusCode.index.toString() +
        ')';
  }
}
