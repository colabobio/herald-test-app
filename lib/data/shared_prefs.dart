import 'package:nanoid/nanoid.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../constants/strings.dart';

class SharedPrefs {
  static late SharedPreferences _sharedPrefs;

  factory SharedPrefs() => SharedPrefs._internal();

  SharedPrefs._internal();

  Future<void> init() async {
    _sharedPrefs = await SharedPreferences.getInstance();
  }

  int get getIdentifier =>
      _sharedPrefs.getInt(identifier) ??
      int.parse(customAlphabet('1234567890', 9));

  setIdentifier(int value) async {
    await _sharedPrefs.setInt(identifier, value);
  }
}
