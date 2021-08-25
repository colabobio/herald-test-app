package com.example.heraldtest;

import android.os.Binder;

import androidx.annotation.NonNull;

public class TestServiceBinder extends Binder {

    @NonNull
    private TestService service;

    TestServiceBinder(@NonNull TestService testService) {
        this.service = testService;
    }

    @NonNull
    public TestService getTestService() {
        return service;
    }
}
