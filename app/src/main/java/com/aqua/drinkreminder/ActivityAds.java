package com.aqua.drinkreminder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ActivityAds extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_no_ads);

        Button btnBuy = findViewById(R.id.button_buy);
        btnBuy.setOnClickListener(this);

        Button btnClose = findViewById(R.id.button_close);
        btnClose.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_buy:
                // open inapp billing
                break;
            case R.id.button_close:
                finish();
                break;
        }
    }
}
