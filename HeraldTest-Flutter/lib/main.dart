import 'home_page.dart';
import 'package:flutter/material.dart';
import 'package:flutterxherald/widgets/app_retain_widget.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'HeraldTestFlutter',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const AppRetainWidget(
        child: HomePage(title: 'HeraldTestFlutter'),
      ),
    );
  }
}
