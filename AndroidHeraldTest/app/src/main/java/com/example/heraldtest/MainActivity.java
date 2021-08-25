package com.example.heraldtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private final BroadcastReceiver statusChangedReceiver = TestBroadcast.statusChangeReceived(this::updateStatus);
    private final BroadcastReceiver statusDetectedReceiver = TestBroadcast.peerDetectReceived(this::updatePeers);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = getApplicationContext();
        Intent startIntent = new Intent(context, TestService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(startIntent);
        } else {
            context.startService(startIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        final LocalBroadcastManager broadcast = LocalBroadcastManager.getInstance(this);
        broadcast.registerReceiver(statusChangedReceiver, TestBroadcast.statusChangeFilter());
        broadcast.registerReceiver(statusDetectedReceiver, TestBroadcast.peerDetectFilter());
    }

    @Override
    protected void onStop() {
        super.onStop();
        final LocalBroadcastManager broadcast = LocalBroadcastManager.getInstance(this);
        broadcast.unregisterReceiver(statusChangedReceiver);
        broadcast.unregisterReceiver(statusDetectedReceiver);
    }

    protected void updateStatus() {
        if (TestService.instance != null) {
            TextView information = findViewById(R.id.status);
            information.setText(TestService.instance.state);
        }
    }

    protected void updatePeers() {
        if (TestService.instance != null) {
            EditText peers = findViewById(R.id.peers);
            for (String peer: TestApplication.instance.peerStatus) {
                peers.setText(peer + "\n");
            }
        }
    }
}