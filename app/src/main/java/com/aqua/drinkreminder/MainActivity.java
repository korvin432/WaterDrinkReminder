package com.aqua.drinkreminder;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.aqua.drinkreminder.di.AppComponent;

import dagger.android.support.DaggerAppCompatActivity;

public class MainActivity extends DaggerAppCompatActivity implements View.OnClickListener {

    private ImageView barImage;
    private Button btnMain;
    private Button btnStatistics;
    private Button btnSettings;


    String[] PERMISSIONS = {
            android.Manifest.permission.WAKE_LOCK,
            android.Manifest.permission.SET_ALARM,
            android.Manifest.permission.RECEIVE_BOOT_COMPLETED,
            android.Manifest.permission.VIBRATE,
            android.Manifest.permission.SYSTEM_ALERT_WINDOW,
            android.Manifest.permission.INTERNET
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentMain fragment = FragmentMain.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment)
                .commit();

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }

        barImage = findViewById(R.id.bar_image);
        btnMain = findViewById(R.id.btnMain);
        btnStatistics = findViewById(R.id.btnReport);
        btnSettings = findViewById(R.id.btnSettings);

        btnMain.setOnClickListener(this);
        btnMain.setBackgroundColor(Color.TRANSPARENT);
        btnStatistics.setOnClickListener(this);
        btnStatistics.setBackgroundColor(Color.TRANSPARENT);
        btnSettings.setOnClickListener(this);
        btnSettings.setBackgroundColor(Color.TRANSPARENT);

    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 12) {
            if (Settings.canDrawOverlays(this)) {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("canDraw", true).apply();
                this.recreate();
            } else {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("canDraw", false).apply();
                this.recreate();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                return;
            }
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnMain:
                barImage.setImageDrawable(getResources().getDrawable(R.drawable.menu_main));
                FragmentMain fragment = FragmentMain.newInstance();
                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment)
                        .commit();
                break;
            case R.id.btnReport:
                barImage.setImageDrawable(getResources().getDrawable(R.drawable.menu_statistics));
                FragmentStatistics fragmentStatistics = FragmentStatistics.newInstance();
                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragmentStatistics)
                        .commit();
                break;
            case R.id.btnSettings:
                barImage.setImageDrawable(getResources().getDrawable(R.drawable.menu_settings));
                FragmentSettings fragmentSettings = FragmentSettings.newInstance();
                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragmentSettings)
                        .commit();
                break;
        }
    }
}
