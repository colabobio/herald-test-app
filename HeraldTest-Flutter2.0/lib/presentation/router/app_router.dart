import 'package:flutter/material.dart';

import '../../core/exceptions/route_exception.dart';
import '../screens/home_screen.dart';
import '../widgets/app_retain_widget.dart';

class AppRouter {
  static const String home = '/';

  const AppRouter._();

  static Route<dynamic> onGenerateRoute(RouteSettings settings) {
    switch (settings.name) {
      case home:
        return MaterialPageRoute(
          builder: (_) => const AppRetainWidget(child: HomeScreen()),
        );
      default:
        throw const RouteException('Route not found!');
    }
  }
}
