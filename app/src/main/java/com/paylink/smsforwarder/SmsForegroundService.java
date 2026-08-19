package com.paylink.smsforwarder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

/**
 * Doesn't do much by itself — its only job is to keep a visible,
 * persistent notification up so Android doesn't kill the app process,
 * which keeps SmsReceiver reliably alive to catch incoming SMS.
 */
public class SmsForegroundService extends Service {

    private static final String CHANNEL_ID = "sms_forwarder_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannelIfNeeded();
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // START_STICKY: if Android still kills us under memory pressure,
        // ask it to restart the service as soon as possible.
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "SMS Forwarder",
                        NotificationManager.IMPORTANCE_LOW
                );
                channel.setDescription("পেমেন্ট SMS ফরওয়ার্ডার চালু থাকলে এখানে দেখাবে।");
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        builder.setContentTitle("SMS ফরওয়ার্ডার চলছে")
                .setContentText("bKash/Nagad পেমেন্ট SMS স্বয়ংক্রিয়ভাবে যাচাই হচ্ছে")
                .setOngoing(true)
                .setSmallIcon(android.R.drawable.stat_notify_sync);
        return builder.build();
    }
}
