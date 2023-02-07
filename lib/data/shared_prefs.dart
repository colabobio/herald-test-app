import 'package:nanoid/nanoid.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:uuid/uuid.dart';

import '../constants/strings.dart';

class SharedPrefs {
  static late SharedPreferences _sharedPrefs;

  factory SharedPrefs() => SharedPrefs._internal();

  SharedPrefs._internal();

  Future<void> init() async {
    _sharedPrefs = await SharedPreferences.getInstance();
  }

  int get getIdentifier => _sharedPrefs.getInt(identifier) ?? getRandomUuid();

  int getRandomUuid() {
    var uuid = Uuid();
    var num = uuid.v4().hashCode;
    return num;
  }

  setIdentifier(int value) async {
    await _sharedPrefs.setInt(identifier, value);
  }
}
