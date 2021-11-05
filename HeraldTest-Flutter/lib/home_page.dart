import 'dart:async';
import 'assets/constants.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class HomePage extends StatefulWidget {
  final String title;
  const HomePage({Key? key, required this.title}) : super(key: key);

  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  //Created method channel.
  static const methodChannel =
      MethodChannel('com.flutterxherald.methodChannel');

  late String _statusString = '';
  late String _peersString = '';

  //Used a timer to call _getStatus() & _get_Peers() every second.
  Timer? timer;

  @override
  void initState() {
    super.initState();
    timer = Timer.periodic(const Duration(seconds: 1), (Timer t) {
      _getStatus();
      _getPeers();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          children: [
            const Padding(
              padding: EdgeInsets.symmetric(horizontal: 20, vertical: 40),
              child: Text(
                'Learning/Testing Herald API in Flutter App through Method Channel.',
                textAlign: TextAlign.center,
              ),
            ),
            ElevatedButton(
                onPressed: _getStatus, child: const Text('Check Status')),
            Text(_statusString),
            const SizedBox(height: 10),
            ElevatedButton(
                onPressed: _getPeers, child: const Text('Check Peers')),
            Text(_peersString),
          ],
        ),
      ),
    );
  }

  Future<void> _getPeers() async {
    String peersString;
    try {
      final String result = await methodChannel.invokeMethod(peers);
      peersString = result;
    } on PlatformException catch (e) {
      peersString = "Failed to get peers: '${e.message}'.";
    }

    setState(() {
      _peersString = peersString;
    });
  }

  Future<void> _getStatus() async {
    String statusString;
    try {
      final String result = await methodChannel.invokeMethod(status);
      statusString = result;
    } on PlatformException catch (e) {
      statusString = "Failed to get status: '${e.message}'.";
    }

    setState(() {
      _statusString = statusString;
    });
  }

  @override
  void dispose() {
    timer?.cancel();
    super.dispose();
  }
}
