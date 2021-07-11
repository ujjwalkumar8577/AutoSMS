package com.ujjwalkumar.autosms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private final Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TimerTask Splash = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    Intent in = new Intent();
                    in.setAction(Intent.ACTION_VIEW);
                    in.setClass(getApplicationContext(), SmsActivity.class);
                    in.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(in);
                    finish();
                });
            }
        };
        timer.schedule(Splash,1000);
    }
}