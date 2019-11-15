package com.aqua.drinkreminder;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.os.ConfigurationCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.widget.WheelView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class FragmentMain extends DaggerFragment implements View.OnClickListener {

    private View bottom;
    private View bottomClicked;
    private View bottomBig;
    private TextView tvProgress;
    private TextView tvNextTime;
    private RelativeLayout nextTimeLayout;
    private int currentVolume;
    private int norm;
    private String normString;
    private ProgressBar bar;
    private int startVolume;
    private int customVolume;
    private int selectedNewVolume;
    private int drinksCount;
    private int interstitialCounter;
    private int launchesCount;
    private boolean goalAchieved;
    private TextView tvCustomVolume;
    private TextView tvAdvice;
    private boolean isAdding;
    private boolean is24format;
    private boolean isMetrics;

    private DrinksAdapter mAdapter;
    @Inject
    SQLiteDatabase mDatabase;
    @Inject
    DBHelper dbHelper;
    private RecyclerView recyclerView;
    private InterstitialAd mInterstitialAd;

    public static FragmentMain newInstance() {
        return new FragmentMain();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        launchesCount = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("launchesCount", 1);
        launchesCount += 1;
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("launchesCount", launchesCount).apply();
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        CardView adviceCard = rootView.findViewById(R.id.advice_card);
        MobileAds.initialize(getContext(), "ca-app-pub-3940256099942544~3347511713");

        if ((launchesCount & 1) != 0) {
            AdView adView = new AdView(getContext());
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

            AdView mAdView = rootView.findViewById(R.id.adView);
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            adviceCard.setVisibility(View.INVISIBLE);
        }

        mInterstitialAd = new InterstitialAd(getContext());
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        final Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        final Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        final Animation slideIn = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
        final Animation slideOut = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_bottom);

        tvAdvice = rootView.findViewById(R.id.advice_text);
        updateAdvice();
        checkNewDay();

        bottom = rootView.findViewById(R.id.bottomL);
        Button btnAdd = bottom.findViewById(R.id.btnAdd);
        btnAdd.setBackgroundColor(Color.TRANSPARENT);
        Button btnScroll = rootView.findViewById(R.id.btnScroll);
        btnScroll.setBackgroundColor(Color.TRANSPARENT);


        isMetrics = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isMetrics", true);

        if (isMetrics) {
            bottomClicked = rootView.findViewById(R.id.bottom_clicked);
        } else {
            bottomClicked = rootView.findViewById(R.id.bottom_clicked_fl);
        }
        bottomBig = rootView.findViewById(R.id.main_bottom_big);

        recyclerView = rootView.findViewById(R.id.drinks_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new DrinksAdapter(getContext(), getAllItems(), new DrinksAdapter.ClickListener() {
            @Override
            public void onPositionClicked(int position, int firstVolume, String date, boolean isDelete) {
                if (!isDelete) {
                    updateDrink(position, firstVolume, date);
                } else {
                    deleteDrink(position, date, firstVolume);
                }
            }
        });
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomClicked.setVisibility(View.VISIBLE);
                bottomClicked.startAnimation(fadeIn);
                bottom.startAnimation(fadeOut);
                bottom.setVisibility(View.INVISIBLE);
            }
        });
        btnScroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomBig.setVisibility(View.VISIBLE);
                bottomBig.startAnimation(slideIn);
                bottom.startAnimation(fadeOut);
                bottom.setVisibility(View.INVISIBLE);
                RelativeLayout.LayoutParams llp = new RelativeLayout.
                        LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                llp.setMargins(0, 290, 0, 50); // llp.setMargins(left, top, right, bottom);
                recyclerView.setLayoutParams(llp);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_TOP, R.id.advice_card);
                recyclerView.setLayoutParams(params);
                recyclerView.startAnimation(slideIn);
                nextTimeLayout.setVisibility(View.INVISIBLE);
            }
        });

        Button btnScrollDown = bottomBig.findViewById(R.id.btnScrollDown);
        btnScrollDown.setBackgroundColor(Color.TRANSPARENT);
        btnScrollDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottom.setVisibility(View.VISIBLE);
                bottom.startAnimation(slideIn);
                bottomBig.startAnimation(slideOut);
                bottomBig.setVisibility(View.INVISIBLE);
                RelativeLayout.LayoutParams llp = new RelativeLayout.
                        LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                llp.setMargins(0, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
                recyclerView.setLayoutParams(llp);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
                params.removeRule(RelativeLayout.ALIGN_TOP);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.height = 160;
                recyclerView.setLayoutParams(params);
                recyclerView.startAnimation(slideOut);
                nextTimeLayout.setVisibility(View.VISIBLE);
                nextTimeLayout.startAnimation(slideOut);
            }
        });

        FrameLayout frameLayout = rootView.findViewById(R.id.frame);
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomClicked.getVisibility() == View.VISIBLE) {
                    bottom.setVisibility(View.VISIBLE);
                    bottom.startAnimation(fadeIn);
                    bottomClicked.startAnimation(fadeOut);
                    bottomClicked.setVisibility(View.INVISIBLE);
                }
            }
        });

        tvProgress = rootView.findViewById(R.id.progress_text);
        tvNextTime = rootView.findViewById(R.id.drink_time);

        nextTimeLayout = rootView.findViewById(R.id.next_layout);

        if (!isMetrics) {
            TextView tvNext = nextTimeLayout.findViewById(R.id.drink_volume);
            tvNext.setText("100 fl");
        }

        String time = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("nextTime", "");
        if (!time.equals("")) {
            nextTimeLayout.setVisibility(View.VISIBLE);
            tvNextTime.setText(time);
        }


        Button btnAdd100 = bottomClicked.findViewById(R.id.btnAdd100);
        btnAdd100.setBackgroundColor(Color.TRANSPARENT);
        btnAdd100.setOnClickListener(this);
        Button btnAdd200 = bottomClicked.findViewById(R.id.btnAdd200);
        btnAdd200.setBackgroundColor(Color.TRANSPARENT);
        btnAdd200.setOnClickListener(this);
        Button btnAdd300 = bottomClicked.findViewById(R.id.btnAdd300);
        btnAdd300.setBackgroundColor(Color.TRANSPARENT);
        btnAdd300.setOnClickListener(this);
        Button btnAdd400 = bottomClicked.findViewById(R.id.btnAdd400);
        btnAdd400.setBackgroundColor(Color.TRANSPARENT);
        btnAdd400.setOnClickListener(this);
        Button btnAdd500 = bottomClicked.findViewById(R.id.btnAdd500);
        btnAdd500.setBackgroundColor(Color.TRANSPARENT);
        btnAdd500.setOnClickListener(this);

        Button btnAddCustom = bottomClicked.findViewById(R.id.btnAddCustom);
        btnAddCustom.setBackgroundColor(Color.TRANSPARENT);
        btnAddCustom.setOnClickListener(this);

        tvCustomVolume = bottomClicked.findViewById(R.id.customText);

        int weight = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("weight", 70);
        boolean isKg = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isKg", true);
        int activity = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("physicalActivity", 1);
        currentVolume = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("currentVolume", 0);
        norm = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("norm", 0);
        customVolume = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("customVolume", -1);
        goalAchieved = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("goalAchieved", false);
        drinksCount = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("drinksCount", 0);
        is24format = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("is24", true);
        interstitialCounter = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("interstitialCounter", 0);

        if (customVolume != -1) {
            tvCustomVolume.setVisibility(View.VISIBLE);
            if (isMetrics) {
                tvCustomVolume.setText(customVolume + " ml");
            } else {
                tvCustomVolume.setText(customVolume + " fl");
            }
        }


        bar = rootView.findViewById(R.id.progress);
        bar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_water));
        bar.setMax(norm);


        if (norm == 0) {
            double normD = getNorm(weight, activity, isKg) * 1000;
            normString = String.valueOf(normD);
            int normA = Integer.valueOf(normString.substring(0, normString.length() - 2));
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                    .putInt("norm", normA).apply();
            bar.setMax(normA);
            if (isMetrics) {
                tvProgress.setText(currentVolume + " ml / " + normString.substring(0, normString.length() - 2) + " ml");
            } else {
                tvProgress.setText(currentVolume + " fl / " + normString.substring(0, normString.length() - 2) + " fl");
            }
        } else {
            normString = String.valueOf(norm);
            if (isMetrics) {
                tvProgress.setText(currentVolume + " ml / " + normString + " ml");
            } else {
                tvProgress.setText(currentVolume + " fl / " + normString + " fl");
            }
        }
        bar.setProgress(currentVolume);

        return rootView;
    }

    private void deleteDrink(int position, String date, final int volume) {
        dbHelper.deleteDrink(position, date);
        currentVolume = currentVolume - volume;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putInt("currentVolume", currentVolume);
        editor.apply();
        mAdapter.notifyDataSetChanged();
        mAdapter = new DrinksAdapter(getContext(), getAllItems(), new DrinksAdapter.ClickListener() {
            @Override
            public void onPositionClicked(int position, int firstVolume, String date, boolean isDelete) {
                if (!isDelete) {
                    updateDrink(position, firstVolume, date);
                } else {
                    deleteDrink(position, date, volume);
                }
            }
        });
        recyclerView.setAdapter(mAdapter);
    }


    private void setAlarm() {
        int interval = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt("notificationInterval", 1);
        long alarmTime;
        if (interval == 30) {
            alarmTime = TimeUnit.MINUTES.toMillis(interval);
        } else {
            alarmTime = TimeUnit.HOURS.toMillis(interval);
        }

        boolean shiftNotifications = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean("shiftNotifications", false);

        long finalTime = System.currentTimeMillis();

        if (shiftNotifications) {
            Date date = new Date(System.currentTimeMillis());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int roundedMinute = ((calendar.get(Calendar.MINUTE) + 5) / 10) * 10;
            calendar.set(Calendar.MINUTE, roundedMinute);
            finalTime = calendar.getTimeInMillis();
        }

        Date date = new Date(finalTime + alarmTime);

        DateFormat formatter;
        if (is24format) {
            formatter = new SimpleDateFormat("HH:mm",
                    ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0));
        } else {
            formatter = new SimpleDateFormat("hh:mm",
                    ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0));
        }
        String nextTime = formatter.format(date);
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("nextTime", nextTime).apply();
        nextTimeLayout.setVisibility(View.VISIBLE);
        tvNextTime.setText(nextTime);

        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) getActivity().getSystemService(getContext().ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, finalTime + alarmTime, alarmTime, alarmIntent);
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("notificationSet", true).apply();


        String wakeUpTime = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString("wakeUpTime", "00:00");

        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.DAY_OF_MONTH, calendar1.get(Calendar.DAY_OF_MONTH) + 1);
        calendar1.set(Calendar.HOUR_OF_DAY, Integer.valueOf((wakeUpTime.substring(0, wakeUpTime.length() - 3))));
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);

        Intent intent2 = new Intent(getContext(), AlarmReceiver.class);
        PendingIntent alarmIntent2 = PendingIntent.getBroadcast(getContext(), 2, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm2 = (AlarmManager) getActivity().getSystemService(getContext().ALARM_SERVICE);
        alarm2.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar1.getTimeInMillis() + alarmTime, AlarmManager.INTERVAL_DAY, alarmIntent2);
    }


    private void checkNewDay() {
        int lastTimeStarted = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt("last_time_started", -1);
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_YEAR);

        Date date = calendar.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
                ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0));
        String strDate = dateFormat.format(date);
        if (today != lastTimeStarted) {
            //dbHelper.clearDrinks();
            updateAdvice();
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            editor.putInt("last_time_started", today);
            if (dbHelper.getVolume(strDate) != null) {
                editor.putInt("currentVolume", Integer.valueOf(dbHelper.getVolume(strDate)));
            } else {
                editor.putInt("currentVolume", 0);
            }
            editor.putBoolean("goalAchieved", false);
            editor.apply();
            dbHelper.addDay(strDate);
        }
    }

    private void updateAdvice() {
        String[] advices = getContext().getResources().getStringArray(R.array.advices);
        String randomAdvice = advices[new Random().nextInt(advices.length)];
        tvAdvice.setText(randomAdvice);
    }

    private void showGoalAchieved() {
        final Dialog dialog = new Dialog(getContext());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_goal_achieved);

        Button continueBtn = dialog.findViewById(R.id.btnCont);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putBoolean("goalAchieved", true);
        editor.apply();
    }

    private Cursor getAllItems() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
                ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0));
        String strDate = dateFormat.format(date);
        return mDatabase.query(
                DBHelper.TABLE_DRINKS,
                null,
                DBHelper.COLUMN.TIME + " LIKE '%" + strDate + "%'",
                null,
                null,
                null,
                DBHelper.COLUMN.ID + " DESC"
        );
    }


    private void updateDrink(final int positions, final int firstVolume, String date) {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_custom_item);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final String dateStr = date;

        final WheelView volumeWheel = dialog.findViewById(R.id.volumeWheel);
        Button applyVolume = dialog.findViewById(R.id.applyVolume);

        volumeWheel.setWheelAdapter(new ArrayWheelAdapter(getContext()));
        volumeWheel.setSkin(WheelView.Skin.Holo);
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.backgroundColor = Color.TRANSPARENT;
        style.selectedTextColor = Color.parseColor("#FFFAF8F8");
        style.holoBorderColor = Color.parseColor("#FF78E2FF");
        style.textColor = Color.parseColor("#FF78E2FF");
        volumeWheel.setStyle(style);
        volumeWheel.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
            @Override
            public void onItemSelected(int position, Object o) {
                selectedNewVolume = Integer.valueOf(o.toString());
            }
        });
        volumeWheel.setWheelData(createVolumes());
        dialog.show();

        applyVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.updateDrink(positions, selectedNewVolume, dateStr);
                dialog.dismiss();
                mAdapter.notifyDataSetChanged();
                mAdapter = new DrinksAdapter(getContext(), getAllItems(), new DrinksAdapter.ClickListener() {
                    @Override
                    public void onPositionClicked(int position, int firstVolume, String date, boolean isDelete) {
                        if (!isDelete) {
                            updateDrink(position, firstVolume, date);
                        } else {
                            deleteDrink(position, date, firstVolume);
                        }
                    }
                });
                recyclerView.setAdapter(mAdapter);
                Calendar calendar = Calendar.getInstance();
                Date date = calendar.getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
                        ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0));
                String strDate = dateFormat.format(date);

                if (selectedNewVolume > firstVolume) {
                    ProgressBarAnimation anim = new ProgressBarAnimation(bar, bar.getProgress(), currentVolume + (selectedNewVolume - firstVolume));
                    anim.setDuration(1000);
                    bar.startAnimation(anim);
                    bar.setProgress(currentVolume + (selectedNewVolume - firstVolume));

                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().
                            putInt("currentVolume", currentVolume + (selectedNewVolume - firstVolume)).apply();
                    currentVolume = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("currentVolume", 0);
                    dbHelper.updateDay(strDate, currentVolume);
                } else {
                    ProgressBarAnimation anim = new ProgressBarAnimation(bar, bar.getProgress(), currentVolume + (selectedNewVolume - firstVolume));
                    anim.setDuration(1000);
                    bar.startAnimation(anim);
                    bar.setProgress(currentVolume - (firstVolume - selectedNewVolume));
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().
                            putInt("currentVolume", currentVolume - (firstVolume - selectedNewVolume)).apply();
                    currentVolume = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("currentVolume", 0);
                    dbHelper.updateDay(strDate, currentVolume);
                }
                if (norm == 0) {
                    if (isMetrics) {
                        tvProgress.setText(currentVolume + " ml / " + normString.substring(0, normString.length() - 2) + " ml");
                    } else {
                        tvProgress.setText(currentVolume + " fl / " + normString.substring(0, normString.length() - 2) + " fl");
                    }
                } else {
                    normString = String.valueOf(norm);
                    if (isMetrics) {
                        tvProgress.setText(currentVolume + " ml / " + normString + " ml");
                    } else {
                        tvProgress.setText(currentVolume + " fl / " + normString + " fl");
                    }
                }
            }
        });

        boolean achieved = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("goalAchieved", false);
        if (currentVolume >= norm && !achieved) {
            showGoalAchieved();
        }
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


    private void addDrink(final int volume) {
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("interstitialCounter", interstitialCounter + 1).apply();
        interstitialCounter += 1;
        if (interstitialCounter >= 3) {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
            interstitialCounter = 0;
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("interstitialCounter", 0).apply();
        }
        if (!isAdding) {
            String curVolume = String.valueOf(volume);

            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                    ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0));
            String strDate = dateFormat.format(date);

            PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                    .putInt("currentVolume", currentVolume + volume).apply();
            startVolume = currentVolume;
            currentVolume = currentVolume + volume;

            DateFormat dateFormatNoHours = new SimpleDateFormat("yyyy-MM-dd",
                    ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0));
            String strDateNoHours = dateFormatNoHours.format(date);
            dbHelper.updateDay(strDateNoHours, currentVolume);
            dbHelper.updateDrinksCount(strDateNoHours);
            if (norm == 0) {
                isAdding = true;
                Runnable runnable = new Runnable() {
                    public void run() {
                        // Переносим сюда старый код
                        int count = 0;
                        for (int i = startVolume; i < currentVolume; i++) {
                            Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            startVolume += 1;
                            int diff = currentVolume - startVolume;
                            if (count <= volume - 2) {
                                count += 1;
                            }
                            bundle.putInt("Key", startVolume);
                            bundle.putBoolean("isFirst", true);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                            if (!goalAchieved) {
                                try {
                                    Thread.sleep(volume / (volume - count) - diff / (volume - count));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        isAdding = false;
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
            } else {
                isAdding = true;
                Runnable runnable = new Runnable() {
                    public void run() {
                        // Переносим сюда старый код
                        int count = 0;
                        for (int i = startVolume; i < currentVolume; i++) {
                            Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            startVolume += 1;
                            int diff = currentVolume - startVolume;
                            if (count <= volume - 2) {
                                count += 1;
                            }
                            bundle.putInt("Key", startVolume);
                            bundle.putBoolean("isFirst", false);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                            if (!goalAchieved) {
                                try {
                                    Thread.sleep(volume / (volume - count) - diff / (volume - count));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        isAdding = false;
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
            }
            dbHelper.addDrink(strDate, curVolume, mAdapter.getItemCount() + 1);
            mAdapter.notifyDataSetChanged();
            mAdapter = new DrinksAdapter(getContext(), getAllItems(), new DrinksAdapter.ClickListener() {
                @Override
                public void onPositionClicked(int position, int firstVolume, String date, boolean isDelete) {
                    if (!isDelete) {
                        updateDrink(position, firstVolume, date);
                    } else {
                        deleteDrink(position, date, firstVolume);
                    }
                }
            });
            recyclerView.setAdapter(mAdapter);
            boolean achieved = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("goalAchieved", false);
            if (currentVolume >= norm && !achieved) {
                showGoalAchieved();
            }
        }
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("drinksCount", drinksCount + 1).apply();
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("notificationWatched", true).apply();
        drinksCount = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("drinksCount", 0);
        setAlarm();
        if (drinksCount == 2) {
            showRateUs();
        }
    }

    private void showRateUs() {
        final Dialog dialog = new Dialog(getContext());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.rate_us);
        Button laterBtn = dialog.findViewById(R.id.btnLater);
        laterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ImageView starsImage = dialog.findViewById(R.id.start_image);
        starsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // click on stars
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            int vol = bundle.getInt("Key");
            boolean isFirst = bundle.getBoolean("isFirst");
            if (!isFirst) {
                if (isMetrics) {
                    tvProgress.setText(startVolume + " ml / " + normString + " ml");
                } else {
                    tvProgress.setText(startVolume + " fl / " + normString + " fl");
                }
            } else {
                if (isMetrics) {
                    tvProgress.setText(startVolume + " ml / " + normString.substring(0, normString.length() - 2) + " ml");
                } else {
                    tvProgress.setText(startVolume + " fl / " + normString.substring(0, normString.length() - 2) + " fl");
                }
            }
            bar.setProgress(vol);
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddCustom:
                final Dialog dialog = new Dialog(getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.layout_custom_item);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                final WheelView volumeWheel = dialog.findViewById(R.id.volumeWheel);
                Button applyVolume = dialog.findViewById(R.id.applyVolume);

                volumeWheel.setWheelAdapter(new ArrayWheelAdapter(getContext()));
                volumeWheel.setSkin(WheelView.Skin.Holo);
                WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
                style.backgroundColor = Color.TRANSPARENT;
                style.selectedTextColor = Color.parseColor("#FFFAF8F8");
                style.holoBorderColor = Color.parseColor("#FF78E2FF");
                style.textColor = Color.parseColor("#FF78E2FF");
                volumeWheel.setStyle(style);
                volumeWheel.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
                    @Override
                    public void onItemSelected(int position, Object o) {
                        customVolume = Integer.valueOf(o.toString());
                    }
                });
                volumeWheel.setWheelData(createVolumes());
                dialog.show();

                applyVolume.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                                .putInt("customVolume", customVolume).apply();
                        dialog.dismiss();
                        if (isMetrics) {
                            tvCustomVolume.setText(customVolume + " ml");
                        } else {
                            tvCustomVolume.setText(customVolume + " fl");
                        }
                        tvCustomVolume.setVisibility(View.VISIBLE);
                    }
                });
                break;
            case R.id.btnAdd100:
                addDrink(100);
                break;
            case R.id.btnAdd200:
                addDrink(200);
                break;
            case R.id.btnAdd300:
                if (customVolume == -1) {
                    addDrink(300);
                } else if (customVolume != 0) {
                    addDrink(customVolume);
                }
                break;
            case R.id.btnAdd400:
                addDrink(400);
                break;
            case R.id.btnAdd500:
                addDrink(500);
                break;
        }
    }


    private ArrayList<String> createVolumes() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 10; i <= 1000; i++) {
            if (String.valueOf(i).substring(String.valueOf(i).length() - 1).equals("0")) {
                list.add(i + "");
            }
        }
        return list;
    }
}
