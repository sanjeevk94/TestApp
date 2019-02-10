package com.scubearena.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;

import in.shadowfax.proswipebutton.ProSwipeButton;

public class StartActivity extends AppCompatActivity {

    private Vibrator myVib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        final ProSwipeButton proSwipeBtn = findViewById(R.id.startlogin);
        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        proSwipeBtn.setSwipeDistance(0.5f);

        proSwipeBtn.setOnSwipeListener(new ProSwipeButton.OnSwipeListener() {
            @Override
            public void onSwipeConfirm() {

                myVib.vibrate(50);
                // user has swiped the btn. Perform your async operation now
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent regIntent = new Intent(StartActivity.this, LoginActivity.class);
                        startActivity(regIntent);
                        finish();
                        proSwipeBtn.showResultIcon(true);
                    }
                }, 2000);

            }

        });

    }
}
