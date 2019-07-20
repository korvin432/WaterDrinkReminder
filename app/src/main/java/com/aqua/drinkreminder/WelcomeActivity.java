package com.aqua.drinkreminder;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        boolean dataCreated = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("dataCreated", false);

        if (dataCreated) {
            launchHomeScreen();
            finish();
        }

        setContentView(R.layout.activity_welcome);

        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentGender fragment = FragmentGender.newInstance();

                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_bottom, R.anim.fade_out_fast)
                        .replace(R.id.container, fragment)
                        .addToBackStack("g").commit();
            }
        });
    }

    private void launchHomeScreen() {
        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        finish();
    }

}
