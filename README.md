# Herald Test Flutter app

Herald & Services in Native. Method & Event channels to communicate. Functionality & UI in Flutter.

## Getting Started

1. Install Flutter: [Flutter](https://docs.flutter.dev/get-started/install)
2. Install VS Code: [VS Code](https://code.visualstudio.com/download)
3. Extensions to install in VS Code: Flutter, Dart, Error Lens
4. Clone the project & open it in VS Code: herald-test/HeraldTest-Flutter2.0
5. Open a new terminal in VS Code: **Ctrl + `**
6. Run " **flutter clean** " then run " **flutter pub get** "
7. Navigate to ios folder: " **cd ios** " and run " **pod install** "
- If you have an issue with pod install, add this line to the top of the pod file: " **source ‘https://github.com/CocoaPods/Specs.git’** " and run " **pod install** " again then navigate back: " **cd ..** "

8. Open up the project in Xcode: ```ios/Runner.xcworkspace```
9. Navigate to Runner --> Runner under targets --> Signing & Capabilities and select your team
10. Head back to VS Code & navigate to the main.dart file in: ```lib```

Project should be able to run now with no issues. Use play button in the top right in VS Code to run the project in debug mode. If you want to run the release version run " **flutter run --release** " in the terminal

## Herald logging

This test app has Herald efficiency logging enabled by default to collect data that is helpful to debug the proximity detection.

* On Android, the log files are saved in the following external storage folder: ```/storage/emulated/0/Android/media/com.example.herald_flutter/Sensor``` so the adb tool from the Android SDK can be used to download those files to the computer:

```$ adb -s <device> pull /storage/emulated/0/Android/media/com.example.herald_flutter/Sensor```

* On iOS, go to "Devices and Emulators" in Xcode and then download the container for the installed HeraldTest-Flutter app. The log files will be inside the ```AppData/Documents``` subfolder inside the package's folder. 
