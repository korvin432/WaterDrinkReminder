package com.aqua.drinkreminder;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static com.aqua.drinkreminder.DBHelper.TAG;

public class StartUpReceiver extends BroadcastReceiver {
    long nextNotificationTime;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            boolean notifSet = prefs.getBoolean("notificationSet", true);
            nextNotificationTime = prefs.getLong("nextNotificationTime", 0);


            int interval = PreferenceManager.getDefaultSharedPreferences(context)
                    .getInt("notificationInterval", 1);
            long alarmTime;
            if (interval == 30){
                alarmTime = TimeUnit.MINUTES.toMillis(interval);
            } else {
                alarmTime = TimeUnit.HOURS.toMillis(interval);
            }

            checkNewDay(context);
            if (notifSet) {
                resetAlarm(context, nextNotificationTime, alarmTime);
            }
        }
    }

    private void checkNewDay(Context context){
        int lastTimeStarted = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt("last_time_started", -1);
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_YEAR);

        String wakeUpTime = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("wakeUpTime", "00:00");

        if (today != lastTimeStarted) {
            Calendar newCalendar = Calendar.getInstance();
            newCalendar.set(Calendar.HOUR_OF_DAY,
                    Integer.valueOf(wakeUpTime.substring(0, wakeUpTime.length() - 3)));
            nextNotificationTime = newCalendar.getTimeInMillis();
        }
    }

    private void resetAlarm(Context context, long time, long alarmTime) {
        createNotificationChannel(context);
        Intent intent = new Intent(context, AlarmReceiver.class);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

        alarm.setRepeating(AlarmManager.RTC_WAKEUP, time, alarmTime, alarmIntent);
    }

    private void createNotificationChannel(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String description = "";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel("1", "notif", importance);
            notificationChannel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
