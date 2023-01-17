import 'package:flutter/material.dart';

import 'data/shared_prefs.dart';
import 'presentation/home_page.dart';
import 'widgets/app_retain_widget.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await SharedPrefs().init();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const AppRetainWidget(child: HomePage()),
    );
  }
}
