import 'dart:async';
import 'package:intl/intl.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:herald_flutter/widgets/peer_info.dart';
import 'package:herald_flutter/widgets/shared_prefs.dart';
import 'package:herald_flutter/widgets/generate_date.dart';
import 'package:herald_flutter/widgets/illness_status_code.dart';

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);
  @override
  State<HomePage> createState() => _HomePageState();
}

final IllnessStatusCode _illnessStatusCode = IllnessStatusCode();
final GenerateDate _generateDate = GenerateDate();

class _HomePageState extends State<HomePage> {
  final MethodChannel _methodChannel =
      const MethodChannel("com.herald_flutter.methodChannel");
  static const String _initalPayload = "initialPayload";
  static const String _removePeer = "removePeer";

  final EventChannel _eventChannel =
      const EventChannel("com.herald_flutter.eventChannel");

  String _uuid = '';
  var _randomIllnessStatusCode = _illnessStatusCode.getRandomStatus();
  String _lastUpdateDate = _generateDate.generateDate();

  final Map<int, PeerInfo> _currentPeers = {};
  String _currentPeersTxt = '';

  final double maxContactDistance = 3; // In meters
  final int maxUpdateWait = 120; // In seconds

  final int statusUpdatePeriod = 30; // In seconds

  @override
  initState() {
    super.initState();
    _sendData();
    _receiveData();
    Timer.periodic(const Duration(milliseconds: 2000), _updateLoop);
  }

  //Method channel to send initial payload data (UUID, code, date)
  Future<void> _sendData() async {
    int uuid = SharedPrefs().getIdentifier;
    _uuid = uuid.toString();
    try {
      await _methodChannel.invokeMethod(_initalPayload, <String, dynamic>{
        'uuid': uuid,
        'illnessStatusCode': _randomIllnessStatusCode.index,
        'date': _lastUpdateDate
      });
    } on PlatformException catch (e) {
      // ignore: avoid_print
      print(e.message);
    }
    SharedPrefs().setIdentifier(uuid);
  }

  Future<void> _sendRemovalCommand(int UUID) async {
    try {
      await _methodChannel.invokeMethod(_removePeer, <String, dynamic>{
        'uuid': UUID,
      });
    } on PlatformException catch (e) {
      // ignore: avoid_print
      print(e.message);
    }
  }

  //Event channel to recieve a stream of peers payload data (UUID, Code, Date, RSSI)
  Future<void> _receiveData() async {
    _eventChannel.receiveBroadcastStream().listen((data) {
      setState(() {
        if (data != null) {
          _storePeersToMap(data);
          _updateCurrentPeersText();
        }
      });
      //Print statements created for testing purposes
      print("******************* UUID: " +
          data['uuid'].toString() +
          " ************************");
      print("******************* CODE: " +
          data['code'].toString() +
          " ************************");
      print("******************* RSSI: " +
          data['rssi'].toString() +
          " ************************");
      print("******************* Samples: " +
          data['samples'].toString() +
          " ************************");
      print("******************* Distance: " +
          data['distance'].toString() +
          " ************************");
      print("******************* CURRENT PEERS: " +
          _currentPeersTxt +
          " *******************");
    });
  }

  void _storePeersToMap(dynamic data) {
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
      info.setRSSI(data["rssi"]);
    }

    if (data["distance"] != null) {
      info.setDistance(data["distance"]);
    }

    if (data["samples"] != null) {
      info.setSampleCount(data["samples"]);
    }

    if (data["phone"] != null) {
      info.setPhoneCode(data["phone"]);
    }

    // If the peer is outside contact range, remove
    if (info.getDistance() > maxContactDistance || data["uuid"] == 1234567890) {
      _sendRemovalCommand(data["uuid"]);
      setState(() {
        _currentPeers.remove(data["uuid"]);
      });
    }
  }

  void _updateCurrentPeersText() {
    String txt = '';
    for (int id in _currentPeers.keys) {
      PeerInfo? info = _currentPeers[id];
      if (info!.getData().isNotEmpty) {
        txt += "->" +
            id.toString() +
            ":Phone=" +
            info.getPhoneCode() +
            ":Status=" +
            info.getIllnessStatus().toString() +
            ":UpdCount:=" +
            info.getUpdateCount() +
            ":RSSI=" +
            info.getRSSI().toString() +
            ":Samples=" +
            info.getSampleCount() +
            ":LastSeen=" +
            info.secondsSinceLastSeen().toString() +
            ":Distance=" +
            info.getDistance().toStringAsFixed(2) +
            "\n";
      }
    }
    _currentPeersTxt = txt;
  }

  void _updateLoop(Timer t) {
    // setState(() {
    //   _lastUpdateDate = _generateDate.generateDate();
    // });
    _updateIllnessStatusCode();
    _removeLostPeers();
  }

  void _updateIllnessStatusCode() {
    String newDate = _generateDate.generateDate();

    int difference = _generateDate.differenceBetweenDates(
        DateFormat('yyyy-MM-dd HH:mm:ss').parse(_lastUpdateDate),
        DateFormat('yyyy-MM-dd HH:mm:ss').parse(newDate));

    if (difference >= statusUpdatePeriod) {
      setState(() {
        _lastUpdateDate = newDate;
        _randomIllnessStatusCode = _illnessStatusCode.getRandomStatus();
      });
      _sendData();
    }
  }

  void _removeLostPeers() {
    List<int> keysToRemove = List<int>.empty(growable: true);
    for (MapEntry e in _currentPeers.entries) {
      PeerInfo info = e.value;
      int difference = info.secondsSinceLastSeen();
      //if no update within the maximum waiting, remove peer from peers map
      if (difference >= maxUpdateWait) {
        keysToRemove.add(e.key);
      }
    }
    for (int key in keysToRemove) {
      _sendRemovalCommand(key);
      setState(() {
        _currentPeers.remove(key);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Center(child: Text('Herald x Flutter 2.0')),
      ),
      body: Center(
        child: Column(
          children: [
            const SizedBox(height: 50),
            const Text(
              'UUID & IllnessStatus',
              style: TextStyle(fontSize: 25),
            ),
            const SizedBox(height: 5),
            Text('$_uuid $_randomIllnessStatusCode (' +
                _randomIllnessStatusCode.index.toString() +
                ')'),
            const SizedBox(height: 20),
            const Text(
              'Peers',
              style: TextStyle(fontSize: 25),
            ),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: Text(_currentPeersTxt),
            ),
          ],
        ),
      ),
    );
  }
}
