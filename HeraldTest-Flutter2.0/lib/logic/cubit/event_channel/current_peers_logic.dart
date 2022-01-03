import 'package:intl/intl.dart';

import '../../../data/peer_info_data.dart';
import '../../generate_date.dart';

class CurrentPeersLogic {
  final GenerateDate _generateDate = GenerateDate();
  final Map<int, PeerInfo> _currentPeers = {};
  String _currentPeersText = '';

  void storePeersToMap(dynamic data) {
    PeerInfo? info = _currentPeers[data["uuid"]];

    if (info == null) {
      info = PeerInfo();
      if (data["uuid"] != null) {
        _currentPeers[data["uuid"]] = info;
      }
    }
    /* if the status is not null && not the same as the current one
    then update the status with the new one */
    if (data["code"] != null && data["code"] != info.getIllnessStatus()) {
      info.setStatus(data["code"]);
    }

    if (data["rssi"] != null) {
      info.addRSSI(data["rssi"]);
    }

    if ((10 <= info.getData().length && info.getRSSI() < -70) ||
        data["uuid"] == 1234567890) {
      _currentPeers.remove(data["uuid"]);
    }
    //Print statements created for testing purposes
    print("******************* UUID: " +
        data['uuid'].toString() +
        " ************************");
    print("******************* CODE: " +
        data['code'].toString() +
        " ************************");
    print("******************* DATE: " +
        data['date'].toString() +
        " ************************");
    print("******************* RSSI: " +
        data['rssi'].toString() +
        " ************************");
    print("******************* CURRENT PEERS: " +
        _currentPeersText +
        " *******************");
    _removeLostPeers();
    _updateCurrentPeersText();
  }

  void _removeLostPeers() {
    for (MapEntry e in _currentPeers.entries) {
      PeerInfo info = e.value;
      DateTime lastSeen = info.getLastSeen();
      DateTime newDateTime = DateFormat('yyyy-MM-dd HH:mm:ss')
          .parse(DateTime.now().toUtc().toString());
      //difference between dates is in seconds
      int difference =
          _generateDate.differenceBetweenDates(lastSeen, newDateTime);
      //if no update in 30 minutes remove peer from peers map
      if (difference >= 1800) {
        _currentPeers.remove(e.key);
      }
    }
  }

  void _updateCurrentPeersText() {
    String text = '';
    for (int id in _currentPeers.keys) {
      PeerInfo? info = _currentPeers[id];

      if (10 <= info!.getData().length) {
        text += "->" +
            id.toString() +
            ":CODE=" +
            info.getIllnessStatus().toString() +
            ":RSSI=" +
            info.getRSSI().toString() +
            ":UPC=" +
            info.getUpdateCount() +
            "\n";
      }
      _currentPeersText = text;
    }
  }

  String getCurrentPeersText() {
    return _currentPeersText;
  }
}
