package com.example.herald_flutter;

import android.os.Binder;
import androidx.annotation.NonNull;

public class TestServiceBinder extends Binder {

    @NonNull
    private TestService service;
    TestServiceBinder(@NonNull TestService testService) {
        this.service = testService;
    }
}
