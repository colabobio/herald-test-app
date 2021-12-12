import 'dart:async';
import 'package:intl/intl.dart';
import 'package:nanoid/nanoid.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:herald_flutter/widgets/peer_info.dart';
import 'package:herald_flutter/widgets/generate_date.dart';
import 'package:herald_flutter/widgets/illness_status_code.dart';

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);
  @override
  State<HomePage> createState() => _HomePageState();
}

final String _randomlyGeneratedUUID = customAlphabet('1234567890', 9);
final IllnessStatusCode _illnessStatusCode = IllnessStatusCode();
final GenerateDate _generateDate = GenerateDate();

class _HomePageState extends State<HomePage> {
  final MethodChannel _methodChannel =
      const MethodChannel("com.herald_flutter.methodChannel");
  static const String _initalPayload = "initialPayload";

  final EventChannel _eventChannel =
      const EventChannel("com.herald_flutter.eventChannel");

  final int _uuid = int.parse(_randomlyGeneratedUUID);
  var _randomIllnessStatusCode = _illnessStatusCode.getRandomStatus();
  String _date = _generateDate.generateDate();

  final Map<int, PeerInfo> _currentPeers = {};
  String _currentPeersTxt = "";

  @override
  initState() {
    super.initState();
    Timer.periodic(const Duration(milliseconds: 2000), _updateLoop);
    _sendData();
    _reciveData();
  }

  //Method channel to send initial payload data (UUID, code, date)
  Future<void> _sendData() async {
    try {
      await _methodChannel.invokeMethod(_initalPayload, <String, dynamic>{
        'uuid': _uuid,
        'illnessStatusCode': _randomIllnessStatusCode.index,
        'date': _date
      });
    } on PlatformException catch (e) {
      // ignore: avoid_print
      print(e.message);
    }
  }

  //Event channel to recieve a stream of peers payload data (UUID, Code, Date, RSSI)
  Future<void> _reciveData() async {
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
      print("******************* DATE: " +
          data['date'].toString() +
          " ************************");
      print("******************* RSSI: " +
          data['rssi'].toString() +
          " ************************");
      print("******************* CURRENT PEERS: " +
          _currentPeersTxt +
          " ************************");
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
    if (data["code"] != null) {
      info.setStatus(data["code"]);
    }

    if (data["rssi"] != null) {
      info.addRSSI(data["rssi"]);
    }

    if (10 <= info.getData().length && info.getRSSI() < -70) {
      _currentPeers.remove(data["uuid"]);
    }
  }

  void _updateLoop(Timer t) {
    _updateIllnessStatusCode();
    _removeLostPeers();
  }

  void _updateIllnessStatusCode() {
    String newDate = _generateDate.generateDate();

    int difference = _generateDate.differenceBetweenDates(
        DateFormat('yyyy-MM-dd HH:mm:ss').parse(_date),
        DateFormat('yyyy-MM-dd HH:mm:ss').parse(newDate));

    if (difference >= 120) {
      setState(() {
        _date = newDate;
        _randomIllnessStatusCode = _illnessStatusCode.getRandomStatus();
      });
      _sendData();
    }
  }

  void _removeLostPeers() {
    for (MapEntry e in _currentPeers.entries) {
      PeerInfo info = e.value;
      DateTime lastSeen = info.getLastSeen();
      DateTime newDateTime = DateFormat('yyyy-MM-dd HH:mm:ss')
          .parse(DateTime.now().toUtc().toString());
      int difference =
          _generateDate.differenceBetweenDates(newDateTime, lastSeen);
      if (difference >= 1800) {
        setState(() {
          _currentPeers.remove(e.key);
        });
      }
    }
  }

  void _updateCurrentPeersText() {
    String txt = '';
    for (int id in _currentPeers.keys) {
      PeerInfo? info = _currentPeers[id];

      if (10 <= info!.getData().length) {
        txt += "->" +
            id.toString() +
            ":CODE=" +
            info.getIllnessStatus().toString() +
            ":RSSI=" +
            info.getRSSI().toString() +
            ":UPC=" +
            info.getUpdateCount() +
            "\n";
      }
      _currentPeersTxt = txt;
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
