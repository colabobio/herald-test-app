package com.example.herald_flutter;

import io.flutter.app.FlutterApplication;

public class TestApplication extends FlutterApplication {

    public static TestApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}