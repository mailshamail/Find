package ru.mailshamail.find.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import ru.mailshamail.find.MainActivity;
import ru.mailshamail.find.R;
import ru.mailshamail.find.addMarkers;
import ru.mailshamail.find.bd.BD;

import static android.app.AlarmManager.ELAPSED_REALTIME;
import static android.os.SystemClock.elapsedRealtime;

public class testService extends Service {

    private static final String TAG = "TestGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000 * 10 * 1;
    private static final float LOCATION_DISTANCE = 0;
    private String name;

    private NotificationManager mNM;
    private int NOTIFICATION = R.string.local_service_started;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(final Location location) {
            Log.e(TAG, "onLocationChanged: " + location);

            final SimpleDateFormat simpDate = new SimpleDateFormat("HH:mm", Locale.getDefault());
            final Date date = new Date(location.getTime());

            final Timestamp da = new Timestamp(date.getTime());
            System.out.println(da);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    addToTable(
                            name,
                            location.getLatitude(),
                            location.getLongitude(),
                            String.valueOf(date.getHours())  + String.valueOf(date.getMinutes())
                    );

                }
            }).start();


            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
            Toast.makeText(getBaseContext(), "Provider: " +provider + " disable", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
            Toast.makeText(getBaseContext(), "Provider: " +provider + " enable", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }


    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        Thread triggerService = new Thread(new Runnable() {
            @SuppressLint("MissingPermission")
            public void run() {
                try {
                    Looper.prepare();

                    name = intent.getStringExtra("name");

                    try {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                                mLocationListeners[1]);

                    } catch (java.lang.SecurityException ex) {
                        Log.i(TAG, "Не удалось обновить местоположение", ex);
                        Toast.makeText(getBaseContext(), "Не удалось обновить местоположение", Toast.LENGTH_LONG).show();
                    } catch (IllegalArgumentException ex) {
                        Log.d(TAG, "Интернет отключен " + ex.getMessage());
                        Toast.makeText(getBaseContext(), "Не удалось обновить местоположение интернет отключен", Toast.LENGTH_LONG).show();
                    }

                    try {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                                mLocationListeners[0]);


                    } catch (java.lang.SecurityException ex) {
                        Log.i(TAG, "Не удалось обновить местоположение", ex);
                        Toast.makeText(getBaseContext(), "Не удалось обновить местоположение", Toast.LENGTH_LONG).show();
                    } catch (IllegalArgumentException ex) {
                        Log.d(TAG, "Интернет отключен " + ex.getMessage());
                        Toast.makeText(getBaseContext(), "Не удалось обновить местоположение интернет отключен", Toast.LENGTH_LONG).show();
                    }
                    Looper.loop();

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }, "LocationThread");
        triggerService.start();
        return START_STICKY;
    }

    private void addToTable(String name, double latitude, double longitude, String date)
    {
        BD bd = new BD();
        try {

            bd.Connect();
            if(bd.getConnection().isClosed())
            {
                bd.Connect();
            }

            if (bd.getConnection() != null)
            {
                bd.addToTable(name, latitude, longitude, date);
            }
        }
        catch (Exception e ){}
    }

    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = getPackageName();
        String channelName = "Gps service";
        NotificationChannel chan = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan.setLightColor(Color.BLUE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        }
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(chan);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.btn_moreinfo)
                .setContentTitle("active")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(2, notification);
    }

    private void showNotification() {

        CharSequence text = getText(R.string.local_service_started);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.btn_moreinfo)
      //          .setTicker(text)
      //          .setWhen(System.currentTimeMillis())
      //          .setContentTitle("active")
      //          .setContentText(text)
                .build();

        startForeground(NOTIFICATION, notification);
        //mNM.notify(1, notification);
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");


        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

       //Notification.Builder builder = new Notification.Builder(this)
       //        .setSmallIcon(R.drawable.btn_moreinfo);
       //Notification notification;
       //if (Build.VERSION.SDK_INT < 16)
       //    notification = builder.getNotification();
       //else
       //    notification = builder.build();
       //startForeground(777, notification);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground();
            System.out.println("1");
        }
        else {
            showNotification();
            System.out.println("2");
        }



        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
            Log.i(TAG, "обновление координат network");
            Toast.makeText(getBaseContext(), "Обновление координат (network)", Toast.LENGTH_SHORT).show();
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "Не удалось обновить", ex);
            Toast.makeText(getBaseContext(), "Обновление координат (network)", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "Интернет не работает, " + ex.getMessage());
            Toast.makeText(getBaseContext(), "Интернет не работает", Toast.LENGTH_SHORT).show();
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
            Log.i(TAG, "обновление координат gps");
            Toast.makeText(getBaseContext(), "Обновление координат (gps)", Toast.LENGTH_LONG).show();
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "Не удалось обновить", ex);
            Toast.makeText(getBaseContext(), "Обновление координат (gps)", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps отключен " + ex.getMessage());
            Toast.makeText(getBaseContext(), "Не удалось обновить (gps)", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());
//
       // PendingIntent restartServicePendingIntent =  PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
       // AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
       // alarmService.set(
       //         AlarmManager.ELAPSED_REALTIME,
       //         SystemClock.elapsedRealtime() + 1000,
       //         restartServicePendingIntent);
//
        Toast.makeText(getBaseContext(), "Процесс убит, перезапуск процесса", Toast.LENGTH_LONG).show();

        startService(restartServiceIntent);

        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        Toast.makeText(this, "service close, restart", Toast.LENGTH_SHORT).show();

        System.out.println("start");
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);


                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }

        startService(new Intent(this, testService.class));

        mNM.cancel(NOTIFICATION);

    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}