import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../core/exceptions/route_exception.dart';
import '../../logic/cubit/event_channel/event_channel_cubit.dart';
import '../../logic/cubit/method_channel/method_channel_cubit.dart';
import '../screens/home_screen.dart';
import '../widgets/app_retain_widget.dart';

class AppRouter {
  static const String home = '/';

  const AppRouter._();

  static Route<dynamic> onGenerateRoute(RouteSettings settings) {
    switch (settings.name) {
      case home:
        return MaterialPageRoute(
          builder: (_) => AppRetainWidget(
              child: MultiBlocProvider(
            providers: [
              BlocProvider(create: (context) => MethodChannelCubit()),
              BlocProvider(create: (context) => EventChannelCubit()),
            ],
            child: const HomeScreen(),
          )),
        );
      default:
        throw const RouteException('Route not found!');
    }
  }
}
