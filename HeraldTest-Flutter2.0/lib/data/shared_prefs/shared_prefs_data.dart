import 'package:nanoid/nanoid.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../core/constants/strings.dart';

class SharedPrefsData {
  static late SharedPreferences _sharedPrefs;

  factory SharedPrefsData() => SharedPrefsData._internal();

  SharedPrefsData._internal();

  Future<void> init() async {
    _sharedPrefs = await SharedPreferences.getInstance();
  }

  int get getIdentifier =>
      _sharedPrefs.getInt(Strings.identifier) ??
      int.parse(customAlphabet('1234567890', 9));

  setIdentifier(int value) async {
    await _sharedPrefs.setInt(Strings.identifier, value);
  }
}
