import 'package:bloc/bloc.dart';

import 'current_peers_logic.dart';

class EventChannelCubit extends Cubit<dynamic> {
  final CurrentPeersLogic _currentPeersLogic = CurrentPeersLogic();
  EventChannelCubit() : super('');

  void reciveCurrentPeersData(dynamic data) {
    _currentPeersLogic.storePeersToMap(data);
    emit(_currentPeersLogic.getCurrentPeersText());
  }
}
