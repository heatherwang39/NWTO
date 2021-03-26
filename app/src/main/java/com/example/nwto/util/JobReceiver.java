package com.example.nwto.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.nwto.NavigationActivity;
import com.example.nwto.R;
import com.example.nwto.api.CrimeApi;
import com.example.nwto.model.Crime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

public class JobReceiver extends BroadcastReceiver {
    private static final String TAG = "TAG: " + JobReceiver.class.getSimpleName();
    private static final String CHANNEL_ID = "NWTO_NOTIFICATION";
    NotificationManager notificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive -> Receiver Called");
        double latitude = intent.getExtras().getDouble("latitude");
        double longitude = intent.getExtras().getDouble("longitude");
        int radius = intent.getExtras().getInt("radius");
        int frequency = intent.getExtras().getInt("frequency");
        readRecentCrimes(context, latitude, longitude, radius, frequency);
    }

    private void readRecentCrimes(Context context, double latitude, double longitude, int radius, int frequency) {
        // calculates start date and end date
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        calendar.add(Calendar.DATE, -frequency);
        Date oldDate = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String endDate = dateFormat.format(currentDate);
        String startDate = dateFormat.format(oldDate);

        int endMonth = Integer.parseInt(endDate.substring(0, 2));
        int endDay = Integer.parseInt(endDate.substring(3, 5));
        int endYear = Integer.parseInt(endDate.substring(6));

        int startMonth = Integer.parseInt(startDate.substring(0, 2));
        int startDay = Integer.parseInt(startDate.substring(3, 5));
        int startYear = Integer.parseInt(startDate.substring(6));

        // queries YTD server
        new CrimeApi() {
            @Override
            public void processCrimes_YTD(List<Crime> crimes) {
                int crimeCounts = crimes.size();
                Log.d(TAG, "readRecentCrimes -> Crime Counts=" + crimeCounts);

                // sends notification
                if (crimeCounts > 0) setNotification(context, crimeCounts, frequency);
            }
        }.queryYTD(radius, latitude, longitude, -1, startYear, startMonth, startDay, endYear, endMonth, endDay, null, null);
    }

    private void createNotificationChannel(Context context) {
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        NotificationChannel notificationChannel = null;

        // Notification channels are only available in OREO and higher.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ID, "NWTO Crime Notification", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notifications from Job Service");
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void setNotification(Context context, int crimeCounts, int frequency) {
        createNotificationChannel(context);

        // Sets up the notification content intent to launch the app when clicked
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, NavigationActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        String title = crimeCounts > 1 ? crimeCounts + " New Crimes" : crimeCounts + " New Crime";
        String text = frequency > 1 ? "has occurred near you during the last " + frequency + " days."
                                    : "has occurred near you during the last " + frequency + " day.";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.landing_logo)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        notificationManager.notify(0, builder.build());
    }
}
