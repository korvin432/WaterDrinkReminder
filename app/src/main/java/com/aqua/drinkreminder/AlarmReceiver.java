package com.aqua.drinkreminder;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.os.ConfigurationCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RemoteViews;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {

    int notificationType;
    String sound;
    int norm;
    boolean keepShowing;
    boolean showWindow;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        boolean notifSet = prefs.getBoolean("notificationSet", true);
        boolean notificationWatched = prefs.getBoolean("notificationWatched", true);
        showWindow = prefs.getBoolean("showWindow", false);
        int interval = prefs.getInt("notificationInterval", 1);
        norm = prefs.getInt("norm", 0);
        notificationType = prefs.getInt("notificationType", 1);
        sound = prefs.getString("sound", "Tab button");
        keepShowing = prefs.getBoolean("keepShowing", false);
        int currentVolume = prefs.getInt("currentVolume", 0);

        String wakeUpTime = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("wakeUpTime", "00:00");
        Date time1 = null;
        try {
            time1 = new SimpleDateFormat("HH:mm", ConfigurationCompat.getLocales(context.getResources()
                    .getConfiguration()).get(0)).parse(wakeUpTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(time1);


        String sleepTime = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("sleepTime", "00:00");
        Date time2 = null;
        try {
            time2 = new SimpleDateFormat("HH:mm", ConfigurationCompat.getLocales(context.getResources()
                    .getConfiguration()).get(0)).parse(sleepTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(time2);
        calendar2.add(Calendar.HOUR, -1);


        Calendar rightNow = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", ConfigurationCompat.getLocales(context.getResources()
                .getConfiguration()).get(0));
        Date time3 = null;
        try {
            time3 = new SimpleDateFormat("HH:mm", ConfigurationCompat.getLocales(context.getResources()
                    .getConfiguration()).get(0)).parse(format.format(rightNow.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(time3);
        Date date = cal.getTime();


        long alarmTime;

        if (interval == 30) {
            alarmTime = TimeUnit.MINUTES.toMillis(interval);
        } else {
            alarmTime = TimeUnit.HOURS.toMillis(interval);
        }

        if (notifSet) {
            if (currentVolume < norm) {
                if (date.after(calendar1.getTime()) && date.before(calendar2.getTime())) {
                    if (notificationWatched) {
                        startAlarm(context, alarmTime, 1);
                        return;
                    }
                }
            } else {
                if (keepShowing) {
                    if (date.after(calendar1.getTime()) && date.before(calendar2.getTime())) {
                        if (notificationWatched) {
                            startAlarm(context, alarmTime, 1);
                            return;
                        }
                    }
                }
            }

            Calendar calendar3 = Calendar.getInstance();
            Calendar calendarNow = Calendar.getInstance();
            calendar3.set(Calendar.HOUR_OF_DAY, Integer.valueOf((wakeUpTime.substring(0, wakeUpTime.length() - 3))) + interval);
            calendar3.set(Calendar.MINUTE, 0);
            calendar3.set(Calendar.SECOND, 0);

            if (calendarNow.get(Calendar.HOUR_OF_DAY) == calendar3.get(Calendar.HOUR_OF_DAY) &&
                calendarNow.get(Calendar.MINUTE) == calendar3.get(Calendar.MINUTE)) {
                startAlarm(context, alarmTime, 2);
            }


        }

    }

    private void startAlarm(Context context, long alarmTime, int requestCode) {
        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

        createNotificationChannel(context, String.valueOf(requestCode), "notif");
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, requestCode, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager myNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, String.valueOf(requestCode));
        } else {
            builder = new Notification.Builder(context);
        }


        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.custom_notification);
        Intent switchIntent = new Intent(context, NotificationClickListener.class);
        PendingIntent notifClick = PendingIntent.getBroadcast(context, 0,
                switchIntent, 0);
        contentView.setOnClickPendingIntent(R.id.notif_layout, notifClick);

        builder.setSmallIcon(R.drawable.notif_icon)
                .setContent(contentView)
                .setContentTitle("Водохлёб")
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());

        if (notificationType != 3) {
            builder.setVibrate(new long[]{1000, 1000});
        }

        if (myKM.inKeyguardRestrictedInputMode() && showWindow) {
            if (notificationType == 1) {
                try {
                    playSound(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (notificationType != 3) {
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(1000);
                }
            }
            createWindow(context);
        } else if (!myKM.inKeyguardRestrictedInputMode() && showWindow) {
            if (notificationType == 1) {
                try {
                    playSound(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            createWindow(context);
        } else if (!showWindow) {
            myNotificationManager.notify(1, builder.build());

            if (notificationType == 1) {
                try {
                    playSound(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = Build.VERSION.SDK_INT >= 20 ? pm.isInteractive() : pm.isScreenOn(); // check if screen is on
            if (!isScreenOn) {
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "myApp:notificationLock");
                wl.acquire(3000); //set your time in milliseconds
            }
        }
        PreferenceManager.getDefaultSharedPreferences(context).edit().
                putLong("nextNotificationTime", System.currentTimeMillis() + alarmTime).apply();
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("notificationWatched", false).apply();
    }

    private void createWindow(final Context context) {
        final WindowManager.LayoutParams params;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
        }

        final WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View myView = inflater.inflate(R.layout.drink_water, null);
        myView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    wm.removeView(myView);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    prefs.edit().putBoolean("notificationWatched", true).apply();
                } else {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    prefs.edit().putBoolean("notificationWatched", true).apply();
                    wm.removeView(myView);
                }
                return false;
            }
        });
        Button btnOpen = myView.findViewById(R.id.btnOpen);
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                prefs.edit().putBoolean("notificationWatched", true).apply();
                wm.removeView(myView);
            }
        });
        wm.addView(myView, params);
    }

    public static class NotificationClickListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putBoolean("notificationWatched", true).apply();

            Intent activityIntent = new Intent(context, MainActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
            NotificationManager myNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
            myNotificationManager.cancelAll();
        }
    }

    private void createNotificationChannel(Context context, String id, String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = "";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel(id, name, importance);
            notificationChannel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void playSound(Context context){
        MediaPlayer mp;
        switch (sound){
            case "Nature water deep":
                mp = MediaPlayer.create(context, R.raw.nature_water_deep);
                mp.start();
                break;
            case "Nature water light movement":
                mp = MediaPlayer.create(context, R.raw.nature_water_light_movement);
                mp.start();
                break;
            case "Stone-drop_effect":
                mp = MediaPlayer.create(context, R.raw.stone_drop_effect);
                mp.start();
                break;
            case "Tab button":
                mp = MediaPlayer.create(context, R.raw.tap_button);
                mp.start();
                break;
            case "Water pour":
                mp = MediaPlayer.create(context, R.raw.water_pour);
                mp.start();
                break;
            case "Water splash":
                mp = MediaPlayer.create(context, R.raw.water_splash);
                mp.start();
                break;
        }
    }
}
