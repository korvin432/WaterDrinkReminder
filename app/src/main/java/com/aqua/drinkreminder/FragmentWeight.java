package com.aqua.drinkreminder;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.widget.WheelView;
import java.util.ArrayList;
import java.util.List;

public class FragmentWeight extends Fragment implements View.OnClickListener {

    private int weight;
    private boolean isKg;

    public static FragmentWeight newInstance() {
        return new FragmentWeight();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weight, container, false);
        ImageView imageViewGender = rootView.findViewById(R.id.gender);

        boolean isMale = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isMale", true);
        if(!isMale){
            imageViewGender.setImageDrawable(getContext().getResources().getDrawable(R.drawable.welcome_3f));
        }

        Button btnNext = rootView.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);
        TextView btnBack = rootView.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        List<String> metrics = new ArrayList<>();
        metrics.add("Kg");
        metrics.add("Lb");

        final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.scroll);

        final WheelView weightWheel = (WheelView) rootView.findViewById(R.id.weightWheel);
        weightWheel.setWheelAdapter(new ArrayWheelAdapter(getContext()));
        weightWheel.setSkin(WheelView.Skin.Holo);
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.backgroundColor = Color.TRANSPARENT;
        style.selectedTextColor = Color.parseColor("#FFFAF8F8");
        style.holoBorderColor = Color.parseColor("#FF78E2FF");
        style.textColor = Color.parseColor("#FF78E2FF");
        style.textSize = 21;
        weightWheel.setStyle(style);
        weightWheel.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
            @Override
            public void onItemSelected(int position, Object o) {
                mp.start();
                weight = Integer.valueOf(o.toString());
            }
        });

        if(isMale){
            weightWheel.setSelection(79);
        } else {
            weightWheel.setSelection(59);
        }
        weightWheel.setWheelData(createKgs());


        final WheelView metricsWheel = (WheelView) rootView.findViewById(R.id.metricsWheel);
        metricsWheel.setWheelAdapter(new ArrayWheelAdapter(getContext()));
        metricsWheel.setSkin(WheelView.Skin.Holo);
        WheelView.WheelViewStyle styleMetricsWheel = new WheelView.WheelViewStyle();
        styleMetricsWheel.backgroundColor = Color.TRANSPARENT;
        styleMetricsWheel.selectedTextColor = Color.parseColor("#FFFAF8F8");
        styleMetricsWheel.holoBorderColor = Color.parseColor("#FF78E2FF");
        styleMetricsWheel.textColor = Color.parseColor("#FF78E2FF");
        styleMetricsWheel.textSize = 21;
        metricsWheel.setStyle(style);
        metricsWheel.setWheelData(metrics);
        metricsWheel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                metricsWheel.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
                    @Override
                    public void onItemSelected(int position, Object o) {
                        mp.start();
                        if (position == 0){
                            int startPosition = weightWheel.getCurrentPosition();
                            weightWheel.resetDataFromTop(createKgs());
                            weightWheel.setSelection(startPosition);
                            isKg = true;
                        } else if (position == 1){
                            int startPosition = weightWheel.getCurrentPosition();
                            weightWheel.resetDataFromTop(createLbs());
                            weightWheel.setSelection(startPosition);
                            isKg = false;
                        }
                    }
                });
                return false;
            }
        });

        return rootView;
    }


    private ArrayList<String> createKgs() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 1; i <= 400; i++) {
            list.add(i + "");
        }
        return list;
    }

    private ArrayList<String> createLbs() {
        ArrayList<String> list = new ArrayList<>();
        List<Integer> kgs = new ArrayList<>();
        for (int i = 1; i <= 400; i++) {
            kgs.add(i);
        }

        for (int i = 0; i < kgs.size(); i++) {
            list.add(Math.round(kgs.get(i) * 2.2) + "");
        }
        return list;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnBack:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.btnNext:
                FragmentActivity fragment = FragmentActivity.newInstance();
                getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_bottom, R.anim.fade_out_fast)
                        .replace(R.id.container, fragment).replace(R.id.container, fragment)
                        .addToBackStack("a").commit();
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("weight", weight).apply();
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isKg", isKg).apply();
                break;
        }
    }
}
