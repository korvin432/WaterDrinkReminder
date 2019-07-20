package com.aqua.drinkreminder;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.widget.WheelView;
import java.util.ArrayList;
import java.util.List;

public class FragmentTime extends Fragment implements View.OnClickListener {

    private static final String ARG_TYPE = "argType";
    private static final String ARG_WAKE_UP = "wakeUpTime";
    private boolean isWakeUp;
    private String wakeUpTime;
    private String wakeUpHour;
    private String wakeUpMinute;
    private String sleepTimeHour;
    private String sleepTimeMinute;

    public static FragmentTime newInstance(boolean isWakeUp, String wakeUpTime) {
        FragmentTime fragment = new FragmentTime();
        Bundle args = new Bundle();
        args.putBoolean(ARG_TYPE, isWakeUp);
        args.putString(ARG_WAKE_UP, wakeUpTime);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_time, container, false);
        if (getArguments() != null) {
            isWakeUp = getArguments().getBoolean(ARG_TYPE);
            wakeUpTime = getArguments().getString(ARG_WAKE_UP);
        }
        ImageView imageViewTime = rootView.findViewById(R.id.image_back_time);
        if (!isWakeUp) {
            imageViewTime.setImageDrawable(getContext().getResources().getDrawable(R.drawable.sleeptime));
            ((TextView) rootView.findViewById(R.id.which_time)).setText(getContext().getResources().getText(R.string.which_time_you_go_sleep));
        }


        Button btnNext = rootView.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);
        TextView btnBack = rootView.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        List<String> hours;

        if (!DateFormat.is24HourFormat(getContext())) {
            hours = createUsaHours();
        } else {
            hours = createEuHours();
        }


        final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.scroll);

        final WheelView hoursWheel = (WheelView) rootView.findViewById(R.id.hoursWheel);
        hoursWheel.setWheelAdapter(new ArrayWheelAdapter(getContext()));
        hoursWheel.setSkin(WheelView.Skin.Holo);
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.backgroundColor = Color.TRANSPARENT;
        style.selectedTextColor = Color.parseColor("#FFFAF8F8");
        style.holoBorderColor = Color.parseColor("#FF78E2FF");
        style.textColor = Color.parseColor("#FF78E2FF");
        style.textSize = 21;
        if (isWakeUp) {
            hoursWheel.setSelection(8);
        } else {
            if (!DateFormat.is24HourFormat(getContext())){
                hoursWheel.setSelection(10);
            } else {
                hoursWheel.setSelection(22);
            }
        }
        hoursWheel.setStyle(style);
        hoursWheel.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
            @Override
            public void onItemSelected(int position, Object o) {
                mp.start();
                if (isWakeUp) {
                    wakeUpHour = o.toString();
                } else {
                    sleepTimeHour = o.toString();
                }
            }
        });
        hoursWheel.setWheelData(hours);


        WheelView minutesWheel = (WheelView) rootView.findViewById(R.id.minutesWheel);
        minutesWheel.setWheelAdapter(new ArrayWheelAdapter(getContext()));
        minutesWheel.setSkin(WheelView.Skin.Holo);
        WheelView.WheelViewStyle styleMetricsWheel = new WheelView.WheelViewStyle();
        styleMetricsWheel.backgroundColor = Color.TRANSPARENT;
        styleMetricsWheel.selectedTextColor = Color.parseColor("#FFFAF8F8");
        styleMetricsWheel.holoBorderColor = Color.parseColor("#FF78E2FF");
        styleMetricsWheel.textColor = Color.parseColor("#FF78E2FF");
        styleMetricsWheel.textSize = 21;
        minutesWheel.setStyle(style);
        minutesWheel.setWheelData(createMinutes());
        minutesWheel.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
            @Override
            public void onItemSelected(int position, Object o) {
                mp.start();
                if (isWakeUp) {
                    wakeUpMinute = o.toString();
                } else {
                    sleepTimeMinute = o.toString();
                }
            }
        });

        return rootView;
    }


    private ArrayList<String> createEuHours() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i <= 23; i++) {
            if (i < 10) {
                list.add("0" + i);
            } else {
                list.add(i + "");
            }
        }
        return list;
    }

    private ArrayList<String> createUsaHours() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i <= 12; i++) {
            if (i < 10) {
                list.add("0" + i);
            } else {
                list.add(i + "");
            }
        }
        return list;
    }

    private ArrayList<String> createMinutes() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i <= 59; i++) {
            if (i < 10) {
                list.add("0" + i);
            } else {
                list.add(i + "");
            }
        }
        return list;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.btnNext:
                if (isWakeUp) {
                    wakeUpTime = wakeUpHour + ":" + wakeUpMinute;
                }
                String sleepTime = sleepTimeHour + ":" + sleepTimeMinute;
                if (isWakeUp) {
                    FragmentTime fragment = FragmentTime.newInstance(false, wakeUpTime);
                    getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_bottom, R.anim.fade_out_fast)
                            .replace(R.id.container, fragment).replace(R.id.container, fragment)
                            .addToBackStack("t1").commit();
                }
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("wakeUpTime", wakeUpTime).apply();
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("sleepTime", sleepTime).apply();
                if (!isWakeUp) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().
                            putBoolean("dataCreated", true).apply();
                }
                break;
        }
    }
}
