import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../core/constants/strings.dart';
import '../../logic/cubit/event_channel/event_channel_cubit.dart';
import '../../logic/cubit/method_channel/method_channel_cubit.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({Key? key}) : super(key: key);
  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final EventChannel _eventChannel =
      const EventChannel(Strings.eventChannelName);

  @override
  initState() {
    super.initState();
    _sendData();
    _reciveData();
    Timer.periodic(const Duration(milliseconds: 2000),
        context.read<MethodChannelCubit>().checkIllnessStatus);
  }

  //Method channel to send initial payload data (UUID, Code, Date)
  Future<void> _sendData() async {
    context.read<MethodChannelCubit>().sendInitialCurrentUserData();
  }

  //Event channel to recieve a stream of peers payload data (UUID, Code, Date, RSSI)
  Future<void> _reciveData() async {
    _eventChannel.receiveBroadcastStream().listen((data) {
      if (data != null) {
        context.read<EventChannelCubit>().reciveCurrentPeersData(data);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Center(child: Text(Strings.homeScreenTitle))),
      body: Center(
        child: Column(
          children: [
            const SizedBox(height: 50),
            const Text(
              'UUID & IllnessStatus',
              style: TextStyle(fontSize: 25),
            ),
            const SizedBox(height: 5),
            BlocBuilder<MethodChannelCubit, dynamic>(
              builder: (context, state) {
                return Text(state);
              },
            ),
            const SizedBox(height: 20),
            const Text(
              'Peers',
              style: TextStyle(fontSize: 25),
            ),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: BlocBuilder<EventChannelCubit, dynamic>(
                builder: (context, state) {
                  return Text(state);
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
