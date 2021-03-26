package com.example.nwto.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

public class Notification {
    private static final String TAG = "TAG: " + Notification.class.getSimpleName();
    private static final int JOB_ID = 1;
    private static final long ONE_DAY_INTERVAL = 24 * 60 * 60 * 1000L;
    private static final long ONE_MIN_INTERVAL = 60 * 1000L;
    private AlarmManager alarmManager;

    public void schedule(Context context, double latitude, double longitude, int radius, int frequency) {
        Intent notifyIntent = new Intent(context, JobReceiver.class);

        notifyIntent.putExtra("latitude", latitude);
        notifyIntent.putExtra("longitude", longitude);
        notifyIntent.putExtra("radius", radius);
        notifyIntent.putExtra("frequency", frequency);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 8) // every day at 8 am
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Log.d(TAG, "schedule: notification is scheduled for " + calendar.getTime().toString() + " and repeats every " + frequency + " days");

        long triggerTime = calendar.getTimeInMillis();
        long repeatInterval = AlarmManager.INTERVAL_DAY * frequency;
        // long repeatInterval = ONE_MIN_INTERVAL * 2; // testing

        PendingIntent notifyPendingIntent = PendingIntent.getBroadcast(context, JOB_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(notifyPendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, repeatInterval, notifyPendingIntent);
    }
}
