import 'dart:async';

import 'package:bloc/bloc.dart';

import 'current_user_logic.dart';

class MethodChannelCubit extends Cubit<dynamic> {
  final CurrentUserLogic _currentUserLogic = CurrentUserLogic();
  MethodChannelCubit() : super('');

  void sendInitialCurrentUserData() {
    _currentUserLogic.sendData();
    emit(_currentUserLogic.getCurrentUserInfoText());
  }

  void checkIllnessStatus(Timer timer) {
    if (_currentUserLogic.updateIllnessStatusCode()) {
      sendInitialCurrentUserData();
    }
  }
}
