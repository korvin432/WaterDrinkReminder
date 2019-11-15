package com.aqua.drinkreminder;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.widget.WheelView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class FragmentSettings extends DaggerFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private TextView tvGender;
    private TextView tvActivity;
    private TextView tvMass;
    private TextView tvWakeUp;
    private TextView tvSleepTime;
    private TextView tvValue;
    private TextView tvFormat;
    private TextView tvInterval;
    private TextView tvNotificationType;
    private TextView tvSound;
    private TextView tipShow;
    private TextView tipKeep;
    private TextView tvLanguage;
    private TextView tvUnit;

    private boolean isMale;
    private boolean isKg;
    private boolean canDraw;
    private int physClicked = 0;
    private int notifClicked = 0;
    private String soundClicked;
    private int intervalClicked = 0;
    private int selectedWeight;
    private int selectedNewNorm;
    private int weight;
    private int physicalActivity;
    private int notificationType;
    private int norm;
    private int currentVolume;
    private String wakeUpTime;
    private String wakeUpHour;
    private String wakeUpMinute;
    private String sleepHour;
    private String sleepMinute;
    private String sleepTime;
    private boolean is24format;

    @Inject
    DBHelper dbHelper;

    public static FragmentSettings newInstance() {
        return new FragmentSettings();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        isMale = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean("isMale", true);
        tvGender = rootView.findViewById(R.id.gends);
        tvGender.setOnClickListener(this);
        if (isMale) {
            tvGender.setText(getContext().getResources().getString(R.string.mal));
        } else {
            tvGender.setText(getContext().getResources().getString(R.string.fem));
        }

        currentVolume = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("currentVolume", 0);
        physicalActivity = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt("physicalActivity", 1);
        tvActivity = rootView.findViewById(R.id.fizs);
        tvActivity.setOnClickListener(this);
        switch (physicalActivity) {
            case 0:
                tvActivity.setText(getContext().getResources().getStringArray(R.array.activity_levels)[0].toLowerCase());
                break;
            case 1:
                tvActivity.setText(getContext().getResources().getStringArray(R.array.activity_levels)[1].toLowerCase());
                break;
            case 2:
                tvActivity.setText(getContext().getResources().getStringArray(R.array.activity_levels)[2].toLowerCase());
                break;
        }

        String soundType = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString("sound", "Tab button");
        tvSound = rootView.findViewById(R.id.notification_sound);
        tvSound.setOnClickListener(this);
        tvSound.setText(soundType);

        notificationType = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt("notificationType", 1);
        tvNotificationType = rootView.findViewById(R.id.notifications_type);
        tvNotificationType.setOnClickListener(this);
        switch (notificationType) {
            case 1:
                tvNotificationType.setText(getContext().getResources().getStringArray(R.array.notification_types)[0].toLowerCase());
                break;
            case 2:
                tvNotificationType.setText(getContext().getResources().getStringArray(R.array.notification_types)[1].toLowerCase());
                break;
            case 3:
                tvNotificationType.setText(getContext().getResources().getStringArray(R.array.notification_types)[2].toLowerCase());
                break;
        }

        int interval = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt("notificationInterval", 1);
        tvInterval = rootView.findViewById(R.id.notifications_intervals);
        tvInterval.setOnClickListener(this);
        if (interval == 30) {
            tvInterval.setText(getContext().getResources().getString(R.string.thirty_minutes));
        } else {
            tvInterval.setText(String.format("%d %s", interval, getContext().getResources().getString(R.string.hour)));
        }

        weight = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("weight", 0);
        isKg = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isKg", false);
        is24format = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("is24", true);
        tvMass = rootView.findViewById(R.id.masss);
        tvMass.setOnClickListener(this);
        if (isKg) {
            tvMass.setText(weight + " kg");
        } else {
            tvMass.setText(weight + " lb");
        }


        wakeUpTime = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString("wakeUpTime", "00:00");
        sleepTime = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString("sleepTime", "00:00");

        tvWakeUp = rootView.findViewById(R.id.time_wakeups);
        tvWakeUp.setOnClickListener(this);
        tvWakeUp.setText(wakeUpTime);

        tvSleepTime = rootView.findViewById(R.id.time_sleeps);
        tvSleepTime.setOnClickListener(this);
        tvSleepTime.setText(sleepTime);

        String language = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString("currentLocale", "ru");
        tvLanguage = rootView.findViewById(R.id.langs);
        tvLanguage.setOnClickListener(this);
        if (language.equals("ru")){
            tvLanguage.setText(getContext().getResources().getString(R.string.russian));
        } else {
            tvLanguage.setText(getContext().getResources().getString(R.string.english));
        }

        final boolean isMetrics = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean("isMetrics", true);
        tvUnit = rootView.findViewById(R.id.time_eds);
        tvUnit.setOnClickListener(this);
        if (isMetrics){
            tvUnit.setText("ml, kg");
        } else {
            tvUnit.setText("lbs, fl oz");
        }

        tipShow = rootView.findViewById(R.id.notifications_v_description);
        tipShow.setVisibility(View.GONE);
        tipKeep = rootView.findViewById(R.id.notifications_c_description);
        tipKeep.setVisibility(View.GONE);

        ImageView showTipShow = rootView.findViewById(R.id.question_v);
        showTipShow.setOnClickListener(this);
        ImageView showtipKeep = rootView.findViewById(R.id.question_c);
        showtipKeep.setOnClickListener(this);

        TextView showBuyAds = rootView.findViewById(R.id.offad);
        showBuyAds.setOnClickListener(this);

        boolean keepShowing = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean("keepShowing", false);
        Switch keepSwitch = rootView.findViewById(R.id.notification_keep);
        if (keepShowing) {
            keepSwitch.setChecked(true);
        } else {
            keepSwitch.setChecked(false);
        }
        keepSwitch.setOnCheckedChangeListener(this);

        boolean showWindow = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean("showWindow", false);
        canDraw = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("canDraw", false);
        Switch showWindowSwitch = rootView.findViewById(R.id.notification_show);
        if (showWindow && canDraw) {
            showWindowSwitch.setChecked(true);
        } else {
            showWindowSwitch.setChecked(false);
        }
        showWindowSwitch.setOnCheckedChangeListener(this);

        boolean shiftNotifications = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean("shiftNotifications", false);
        Switch shiftSwitch = rootView.findViewById(R.id.notification_shift);
        if (shiftNotifications) {
            shiftSwitch.setChecked(true);
        } else {
            shiftSwitch.setChecked(false);
        }
        shiftSwitch.setOnCheckedChangeListener(this);

        norm = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("norm", 0);
        tvValue = rootView.findViewById(R.id.values);
        if (isMetrics) {
            tvValue.setText(norm + " ml");
        } else {
            tvValue.setText(norm + " fl");
        }
        tvValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog normDialog = new Dialog(getContext());
                normDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                normDialog.setContentView(R.layout.layout_custom_item);
                normDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                final WheelView volumeWheel = normDialog.findViewById(R.id.volumeWheel);
                Button applyNormVolume = normDialog.findViewById(R.id.applyVolume);

                volumeWheel.setWheelAdapter(new ArrayWheelAdapter(getContext()));
                volumeWheel.setSkin(WheelView.Skin.Holo);
                WheelView.WheelViewStyle styleNorm = new WheelView.WheelViewStyle();
                styleNorm.backgroundColor = Color.TRANSPARENT;
                styleNorm.selectedTextColor = Color.parseColor("#FFFAF8F8");
                styleNorm.holoBorderColor = Color.parseColor("#FF78E2FF");
                styleNorm.textColor = Color.parseColor("#FF78E2FF");
                volumeWheel.setStyle(styleNorm);
                volumeWheel.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
                    @Override
                    public void onItemSelected(int position, Object o) {
                        selectedNewNorm = Integer.valueOf(o.toString());
                    }
                });
                volumeWheel.setWheelData(createVolumes());
                volumeWheel.setSelection((Integer.valueOf(tvValue.getText()
                        .toString().substring(0, tvValue.getText().toString().length() - 3)))/10);
                normDialog.show();

                applyNormVolume.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                                .putInt("norm", selectedNewNorm).apply();
                        normDialog.dismiss();
                        if (isMetrics) {
                            tvValue.setText(selectedNewNorm + " ml");
                        } else {
                            tvValue.setText(selectedNewNorm + " fl");
                        }
                    }
                });
            }
        });

        tvFormat = rootView.findViewById(R.id.time_formats);
        if (is24format) {
            tvFormat.setText("24");
        } else {
            tvFormat.setText("12");
        }
        tvFormat.setOnClickListener(this);

        TextView tvTerms = rootView.findViewById(R.id.terms);
        tvTerms.setOnClickListener(this);
        TextView tvPrivacy = rootView.findViewById(R.id.privacy);
        tvPrivacy.setOnClickListener(this);

        return rootView;
    }

    private double getNorm(int weight, int activity, boolean isKg) {
        int curWeight = (int) Math.round(weight / 10.0) * 10;
        double norm = 0;
        if (!isKg) {
            curWeight = (int) Math.round(Math.round(weight / 2.2) / 10.0) * 10;
        }

        if (curWeight < 50) {
            curWeight = 50;
        } else if (curWeight > 100) {
            curWeight = 100;
        }

        if (curWeight == 50 && activity == 0) {
            norm = 1.55;
        } else if (curWeight == 50 && activity == 1) {
            norm = 2.00;
        } else if (curWeight == 50 && activity == 2) {
            norm = 2.30;
        }

        if (curWeight == 60 && activity == 0) {
            norm = 1.85;
        } else if (curWeight == 60 && activity == 1) {
            norm = 2.30;
        } else if (curWeight == 60 && activity == 2) {
            norm = 2.65;
        }

        if (curWeight == 70 && activity == 0) {
            norm = 2.20;
        } else if (curWeight == 70 && activity == 1) {
            norm = 2.55;
        } else if (curWeight == 70 && activity == 2) {
            norm = 3.00;
        }

        if (curWeight == 80 && activity == 0) {
            norm = 2.50;
        } else if (curWeight == 80 && activity == 1) {
            norm = 2.95;
        } else if (curWeight == 80 && activity == 2) {
            norm = 3.30;
        }

        if (curWeight == 90 && activity == 0) {
            norm = 2.80;
        } else if (curWeight == 80 && activity == 1) {
            norm = 3.30;
        } else if (curWeight == 80 && activity == 2) {
            norm = 3.60;
        }

        if (curWeight == 100 && activity == 0) {
            norm = 3.10;
        } else if (curWeight == 100 && activity == 1) {
            norm = 3.60;
        } else if (curWeight == 100 && activity == 2) {
            norm = 3.90;
        }

        return norm;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gends:
                if (isMale) {
                    tvGender.setText(getContext().getResources().getString(R.string.fem));
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isMale", false).apply();
                } else {
                    tvGender.setText(getContext().getResources().getString(R.string.mal));
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isMale", true).apply();
                }
                isMale = PreferenceManager.getDefaultSharedPreferences(getContext())
                        .getBoolean("isMale", true);
                break;
            case R.id.fizs:
                switch (physClicked) {
                    case 0:
                        tvActivity.setText(getContext().getResources().getStringArray(R.array.activity_levels)[1]);
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("physicalActivity", 1).apply();
                        physicalActivity = 1;
                        physClicked += 1;
                        break;
                    case 1:
                        tvActivity.setText(getContext().getResources().getStringArray(R.array.activity_levels)[2]);
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("physicalActivity", 2).apply();
                        physicalActivity = 2;
                        physClicked += 1;
                        break;
                    case 2:
                        tvActivity.setText(getContext().getResources().getStringArray(R.array.activity_levels)[0]);
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("physicalActivity", 0).apply();
                        physicalActivity = 0;
                        physClicked = 0;
                        break;
                }
                updateNorm();
                break;
            case R.id.notification_sound:
                final Dialog soundDialog = new Dialog(getContext());
                soundDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                soundDialog.setContentView(R.layout.layout_custom_item);
                soundDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                final WheelView soundWheel = soundDialog.findViewById(R.id.volumeWheel);
                Button applySoundVolume = soundDialog.findViewById(R.id.applyVolume);
                TextView headers = soundDialog.findViewById(R.id.choose);
                headers.setText("Выберите звук");


                soundWheel.setWheelAdapter(new ArrayWheelAdapter(getContext()));
                soundWheel.setSkin(WheelView.Skin.Holo);
                WheelView.WheelViewStyle soundStyle = new WheelView.WheelViewStyle();
                soundStyle.backgroundColor = Color.TRANSPARENT;
                soundStyle.selectedTextColor = Color.parseColor("#FFFAF8F8");
                soundStyle.holoBorderColor = Color.parseColor("#FF78E2FF");
                soundStyle.textColor = Color.parseColor("#FF78E2FF");
                soundWheel.setStyle(soundStyle);
                soundWheel.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
                    @Override
                    public void onItemSelected(int position, Object o) {
                        MediaPlayer mp;
                        switch (position) {
                            case 0:
                                mp = MediaPlayer.create(getContext(), R.raw.nature_water_deep);
                                mp.start();
                                break;
                            case 1:
                                mp = MediaPlayer.create(getContext(), R.raw.nature_water_light_movement);
                                mp.start();
                                break;
                            case 2:
                                mp = MediaPlayer.create(getContext(), R.raw.stone_drop_effect);
                                mp.start();
                                break;
                            case 3:
                                mp = MediaPlayer.create(getContext(), R.raw.tap_button);
                                mp.start();
                                break;
                            case 4:
                                mp = MediaPlayer.create(getContext(), R.raw.water_pour);
                                mp.start();
                                break;
                            case 5:
                                mp = MediaPlayer.create(getContext(), R.raw.water_splash);
                                mp.start();
                                break;
                        }
                        soundClicked = o.toString();
                    }
                });
                soundWheel.setWheelData(createSounds());
                soundDialog.show();

                applySoundVolume.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("sound", soundClicked).apply();
                        tvSound.setText(soundClicked);
                        soundDialog.dismiss();
                    }
                });
                break;
            case R.id.notifications_type:
                switch (notifClicked) {
                    case 0:
                        tvNotificationType.setText(getContext().getResources().getStringArray(R.array.notification_types)[0]);
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("notificationType", 1).apply();
                        notificationType = 1;
                        notifClicked += 1;
                        break;
                    case 1:
                        tvNotificationType.setText(getContext().getResources().getStringArray(R.array.notification_types)[1]);
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("notificationType", 2).apply();
                        notificationType = 2;
                        notifClicked += 1;
                        break;
                    case 2:
                        tvNotificationType.setText(getContext().getResources().getStringArray(R.array.notification_types)[2]);
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("notificationType", 3).apply();
                        notificationType = 3;
                        notifClicked = 0;
                        break;
                }
                break;
            case R.id.notifications_intervals:
                final Dialog intervalsDialog = new Dialog(getContext());
                intervalsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                intervalsDialog.setContentView(R.layout.layout_custom_item);
                intervalsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                final WheelView intervalWheel = intervalsDialog.findViewById(R.id.volumeWheel);
                Button applyIntervalVolume = intervalsDialog.findViewById(R.id.applyVolume);
                TextView header = intervalsDialog.findViewById(R.id.choose);
                header.setText(getContext().getResources().getString(R.string.select_interval));

                intervalWheel.setWheelAdapter(new ArrayWheelAdapter(getContext()));
                intervalWheel.setSkin(WheelView.Skin.Holo);
                WheelView.WheelViewStyle intervalStyle = new WheelView.WheelViewStyle();
                intervalStyle.backgroundColor = Color.TRANSPARENT;
                intervalStyle.selectedTextColor = Color.parseColor("#FFFAF8F8");
                intervalStyle.holoBorderColor = Color.parseColor("#FF78E2FF");
                intervalStyle.textColor = Color.parseColor("#FF78E2FF");
                intervalWheel.setStyle(intervalStyle);
                intervalWheel.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
                    @Override
                    public void onItemSelected(int position, Object o) {
                        intervalClicked = position;
                        if (intervalClicked == 0) {
                            intervalClicked = 30;
                        }
                    }
                });
                intervalWheel.setWheelData(createIntervals());
                intervalsDialog.show();

                applyIntervalVolume.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (intervalClicked != 30) {
                            tvInterval.setText(intervalClicked + " часа");
                            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().
                                    putInt("notificationInterval", intervalClicked).apply();
                        } else {
                            tvInterval.setText(getContext().getResources().getString(R.string.thirty_minutes));
                            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().
                                    putInt("notificationInterval", 30).apply();
                        }
                        intervalsDialog.dismiss();
                    }
                });
                break;
            case R.id.masss:
                final Dialog dialog = new Dialog(getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.layout_weight_select);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                List<String> metrics = new ArrayList<>();
                metrics.add("Kg");
                metrics.add("Lb");

                final WheelView weightWheel = dialog.findViewById(R.id.weightWheel);
                Button applyVolume = dialog.findViewById(R.id.applyVolume);

                weightWheel.setWheelAdapter(new ArrayWheelAdapter(getContext()));
                weightWheel.setSkin(WheelView.Skin.Holo);
                WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
                style.backgroundColor = Color.TRANSPARENT;
                style.selectedTextColor = Color.parseColor("#FFFAF8F8");
                style.holoBorderColor = Color.parseColor("#FF78E2FF");
                style.textColor = Color.parseColor("#FF78E2FF");
                weightWheel.setStyle(style);
                weightWheel.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
                    @Override
                    public void onItemSelected(int position, Object o) {
                        selectedWeight = Integer.valueOf(o.toString());
                    }
                });
                weightWheel.setWheelData(createKgs());

                WheelView metricsWheel = (WheelView) dialog.findViewById(R.id.metricsWheel);
                metricsWheel.setWheelAdapter(new ArrayWheelAdapter(getContext()));
                metricsWheel.setSkin(WheelView.Skin.Holo);
                WheelView.WheelViewStyle styleMetricsWheel = new WheelView.WheelViewStyle();
                styleMetricsWheel.backgroundColor = Color.TRANSPARENT;
                styleMetricsWheel.selectedTextColor = Color.parseColor("#FFFAF8F8");
                styleMetricsWheel.holoBorderColor = Color.parseColor("#FF78E2FF");
                styleMetricsWheel.textColor = Color.parseColor("#FF78E2FF");
                metricsWheel.setStyle(style);
                metricsWheel.setWheelData(metrics);
                metricsWheel.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
                    @Override
                    public void onItemSelected(int position, Object o) {
                        if (position == 0) {
                            weightWheel.resetDataFromTop(createKgs());
                            isKg = true;
                        } else if (position == 1) {
                            weightWheel.resetDataFromTop(createLbs());
                            isKg = false;
                        }
                    }
                });
                dialog.show();

                applyVolume.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("weight", selectedWeight).apply();
                        weight = selectedWeight;
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isKg", isKg).apply();
                        dialog.dismiss();
                        if (isKg) {
                            tvMass.setText(selectedWeight + " kg");
                        } else {
                            tvMass.setText(selectedWeight + " lb");
                        }
                        isKg = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isKg", false);
                        physicalActivity = PreferenceManager.getDefaultSharedPreferences(getContext())
                                .getInt("physicalActivity", 1);

                        double normD = getNorm(weight, physicalActivity, isKg) * 1000;
                        String normString = String.valueOf(normD);
                        int normA = Integer.valueOf(normString.substring(0, normString.length() - 2));
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                                .putInt("norm", normA).apply();
                        tvValue.setText(String.valueOf(normA));
                    }
                });
                break;
            case R.id.time_wakeups:
                List<String> hours;

                if (!is24format) {
                    hours = createUsaHours();
                } else {
                    hours = createEuHours();
                }

                final Dialog timeDialog = new Dialog(getContext());
                timeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                timeDialog.setContentView(R.layout.layout_weight_select);
                timeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                Button apply = timeDialog.findViewById(R.id.applyVolume);
                ((TextView) timeDialog.findViewById(R.id.choose)).setText(getContext().getResources().getString(R.string.select_time));

                final WheelView hoursWheel = (WheelView) timeDialog.findViewById(R.id.weightWheel);
                hoursWheel.setWheelAdapter(new ArrayWheelAdapter(getContext()));
                hoursWheel.setSkin(WheelView.Skin.Holo);
                WheelView.WheelViewStyle hourStyle = new WheelView.WheelViewStyle();
                hourStyle.backgroundColor = Color.TRANSPARENT;
                hourStyle.selectedTextColor = Color.parseColor("#FFFAF8F8");
                hourStyle.holoBorderColor = Color.parseColor("#FF78E2FF");
                hourStyle.textColor = Color.parseColor("#FF78E2FF");
                hoursWheel.setStyle(hourStyle);
                hoursWheel.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
                    @Override
                    public void onItemSelected(int position, Object o) {
                        wakeUpHour = o.toString();
                    }
                });
                hoursWheel.setWheelData(hours);
                hoursWheel.setSelection(Integer.valueOf(tvWakeUp.getText().toString().substring(0, tvWakeUp.getText().toString().length() - 3)));

                WheelView minutesWheel = (WheelView) timeDialog.findViewById(R.id.metricsWheel);
                minutesWheel.setWheelAdapter(new ArrayWheelAdapter(getContext()));
                minutesWheel.setSkin(WheelView.Skin.Holo);
                WheelView.WheelViewStyle styleMinutesWheel = new WheelView.WheelViewStyle();
                styleMinutesWheel.backgroundColor = Color.TRANSPARENT;
                styleMinutesWheel.selectedTextColor = Color.parseColor("#FFFAF8F8");
                styleMinutesWheel.holoBorderColor = Color.parseColor("#FF78E2FF");
                styleMinutesWheel.textColor = Color.parseColor("#FF78E2FF");
                minutesWheel.setStyle(styleMinutesWheel);
                minutesWheel.setWheelData(createMinutes());
                minutesWheel.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
                    @Override
                    public void onItemSelected(int position, Object o) {
                        wakeUpMinute = o.toString();
                    }
                });

                timeDialog.show();

                apply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        wakeUpTime = wakeUpHour + ":" + wakeUpMinute;
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("wakeUpTime", wakeUpTime).apply();
                        tvWakeUp.setText(wakeUpTime);
                        timeDialog.dismiss();
                    }
                });
                break;
            case R.id.time_sleeps:
                List<String> sleepHours;

                if (!is24format) {
                    sleepHours = createUsaHours();
                } else {
                    sleepHours = createEuHours();
                }

                final Dialog sleepTimeDialog = new Dialog(getContext());
                sleepTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                sleepTimeDialog.setContentView(R.layout.layout_weight_select);
                sleepTimeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                Button applyS = sleepTimeDialog.findViewById(R.id.applyVolume);
                ((TextView) sleepTimeDialog.findViewById(R.id.choose)).setText(getContext().getResources().getString(R.string.select_time));

                final WheelView hoursWheels = (WheelView) sleepTimeDialog.findViewById(R.id.weightWheel);
                hoursWheels.setWheelAdapter(new ArrayWheelAdapter(getContext()));
                hoursWheels.setSkin(WheelView.Skin.Holo);
                WheelView.WheelViewStyle hourStyles = new WheelView.WheelViewStyle();
                hourStyles.backgroundColor = Color.TRANSPARENT;
                hourStyles.selectedTextColor = Color.parseColor("#FFFAF8F8");
                hourStyles.holoBorderColor = Color.parseColor("#FF78E2FF");
                hourStyles.textColor = Color.parseColor("#FF78E2FF");
                hoursWheels.setStyle(hourStyles);
                hoursWheels.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
                    @Override
                    public void onItemSelected(int position, Object o) {
                        sleepHour = o.toString();
                    }
                });
                hoursWheels.setWheelData(sleepHours);
                hoursWheels.setSelection(Integer.valueOf(tvSleepTime.getText()
                        .toString().substring(0, tvSleepTime.getText().toString().length() - 3)));

                WheelView minutesWheels = (WheelView) sleepTimeDialog.findViewById(R.id.metricsWheel);
                minutesWheels.setWheelAdapter(new ArrayWheelAdapter(getContext()));
                minutesWheels.setSkin(WheelView.Skin.Holo);
                WheelView.WheelViewStyle styleMinutesWheels = new WheelView.WheelViewStyle();
                styleMinutesWheels.backgroundColor = Color.TRANSPARENT;
                styleMinutesWheels.selectedTextColor = Color.parseColor("#FFFAF8F8");
                styleMinutesWheels.holoBorderColor = Color.parseColor("#FF78E2FF");
                styleMinutesWheels.textColor = Color.parseColor("#FF78E2FF");
                minutesWheels.setStyle(styleMinutesWheels);
                minutesWheels.setWheelData(createMinutes());
                minutesWheels.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
                    @Override
                    public void onItemSelected(int position, Object o) {
                        sleepMinute = o.toString();
                    }
                });

                sleepTimeDialog.show();

                applyS.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sleepTime = sleepHour + ":" + sleepMinute;
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("sleepTime", sleepTime).apply();
                        tvSleepTime.setText(sleepTime);
                        sleepTimeDialog.dismiss();
                    }
                });
                break;
            case R.id.time_formats:
                is24format = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("is24", true);
                if (is24format) {
                    tvFormat.setText("12");
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("is24", false).apply();
                } else {
                    tvFormat.setText("24");
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("is24", true).apply();
                }
                break;
            case R.id.question_v:
                tipShow.setVisibility(View.VISIBLE);
                break;
            case R.id.question_c:
                tipKeep.setVisibility(View.VISIBLE);
                break;
            case R.id.offad:
                Intent intent = new Intent(getActivity(), ActivityAds.class);
                startActivity(intent);
                break;
            case R.id.terms:
                final Dialog dialogTerm = new Dialog(getContext());
                dialogTerm.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogTerm.setContentView(R.layout.layout_info);
                dialogTerm.show();
                break;
            case R.id.privacy:
                final Dialog dialogPrivacy = new Dialog(getContext());
                dialogPrivacy.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogPrivacy.setContentView(R.layout.layout_info);
                ((TextView) dialogPrivacy.findViewById(R.id.info_text)).setText(
                        getContext().getResources().getString(R.string.privacy_policy)
                );
                dialogPrivacy.show();
                break;
            case R.id.langs:
                if (tvLanguage.getText().toString().equals(getContext().getResources().getString(R.string.russian))) {
                    Locale locale = new Locale("en");
                    setLocale(locale);
                    tvLanguage.setText(getContext().getResources().getString(R.string.english));
                } else if (tvLanguage.getText().toString().equals(getContext().getResources().getString(R.string.english))){
                    Locale locale = new Locale("ru");
                    setLocale(locale);
                    tvLanguage.setText(getContext().getResources().getString(R.string.russian));
                }
                break;
            case R.id.time_eds:
                if (tvUnit.getText().toString().equals("ml, kg")){
                    tvUnit.setText("lbs, fl oz");
                    changeUnitSystem(false);
                } else {
                    tvUnit.setText("ml, kg");
                    changeUnitSystem(true);
                }
                Intent intent1 = new Intent(getContext(), MainActivity.class);
                startActivity(intent1);
                break;
        }

    }

    private void changeUnitSystem(boolean toMetrics){
        if (toMetrics){
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isMetrics", true).apply();
            norm = (int) (norm * 29.5);
            currentVolume = (int) (currentVolume * 29.5);
            isKg = true;
            weight = (int) (weight / 2.2);
            dbHelper.changeData(true);
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("norm", norm).apply();
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isKg", isKg).apply();
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("weight", weight).apply();
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("currentVolume", currentVolume).apply();
        } else {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isMetrics", false).apply();
            norm = (int) (norm / 29.5);
            currentVolume = (int) (currentVolume / 29.5);
            isKg = false;
            weight = (int) (weight * 2.2);
            dbHelper.changeData(false);
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("norm", norm).apply();
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isKg", isKg).apply();
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("weight", weight).apply();
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("currentVolume", currentVolume).apply();
        }
    }

    public void updateNorm() {
        isKg = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isKg", false);
        physicalActivity = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt("physicalActivity", 1);

        double normD = getNorm(weight, physicalActivity, isKg) * 1000;
        String normString = String.valueOf(normD);
        int normA = Integer.valueOf(normString.substring(0, normString.length() - 2));
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                .putInt("norm", normA).apply();
        tvValue.setText(normA + "");
    }

    private ArrayList<String> createVolumes() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i <= 4000; i++) {
            if (String.valueOf(i).substring(String.valueOf(i).length() - 1).equals("0")) {
                list.add(i + "");
            }
        }
        return list;
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

    private ArrayList<String> createKgs() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i <= 130; i++) {
            list.add(i + "");
        }
        return list;
    }

    private ArrayList<String> createLbs() {
        ArrayList<String> list = new ArrayList<>();
        List<Integer> kgs = new ArrayList<>();
        for (int i = 0; i <= 130; i++) {
            kgs.add(i);
        }

        for (int i = 0; i < kgs.size(); i++) {
            list.add(Math.round(kgs.get(i) * 2.2) + "");
        }
        return list;
    }

    private ArrayList<String> createIntervals() {
        ArrayList<String> list = new ArrayList<>();
        list.add(getContext().getResources().getString(R.string.thirty_minutes));
        for (int i = 1; i <= 10; i++) {
            list.add(i + " " + getContext().getResources().getString(R.string.hour));
        }
        return list;
    }

    private ArrayList<String> createSounds() {
        ArrayList<String> list = new ArrayList<>();
        list.add("Nature water deep");
        list.add("Nature water light movement");
        list.add("Stone-drop_effect");
        list.add("Tap button");
        list.add("Water pour");
        list.add("Water splash");
        return list;
    }

    @SuppressWarnings("deprecation")
    private void setLocale(Locale locale) {
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                .putString("currentLocale", locale.getLanguage()).apply();
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getContext().getApplicationContext().createConfigurationContext(configuration);
        } else {
            resources.updateConfiguration(configuration, displayMetrics);
        }

        if (locale.getLanguage().equals("ru")){
            tvLanguage.setText(getContext().getResources().getString(R.string.russian));
        } else {
            tvLanguage.setText(getContext().getResources().getString(R.string.english));
        }

        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.notification_keep:
                if (isChecked) {
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("keepShowing", true).apply();
                } else {
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("keepShowing", false).apply();
                }
                break;
            case R.id.notification_show:
                if (isChecked) {
                    checkDrawOverlayPermission();
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("showWindow", true).apply();
                } else {
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("showWindow", false).apply();
                }
                break;
            case R.id.notification_shift:
                if (isChecked) {
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("shiftNotifications", true).apply();
                } else {
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("shiftNotifications", false).apply();
                }
                break;
        }
    }

    public void checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getContext().getPackageName()));
                startActivityForResult(intent, 12);
            }
        }
    }


}
