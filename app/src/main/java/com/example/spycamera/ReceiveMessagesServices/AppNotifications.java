package com.example.spycamera.ReceiveMessagesServices;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;

import androidx.core.app.NotificationCompat;

import com.example.spycamera.MainActivity;
import com.example.spycamera.R;

public class AppNotifications extends Application {
    public static final String CHANNEL_ID = "spy_camera_notification";

    @Override
    public void onCreate() {
        super.onCreate();

        StrictMode.VmPolicy.Builder sb = null;
        sb = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(sb.build());
        sb.detectFileUriExposure();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID, "Spy Camera",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }
    }

    public static Notification getNotification(Context context) {

        StrictMode.VmPolicy.Builder sb = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(sb.build());
        sb.detectFileUriExposure();
        PendingIntent pendingIntent;
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int flags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        pendingIntent = PendingIntent.getActivity(context, 1, intent, flags);


        NotificationCompat.Builder notificationBuilder;
        notificationBuilder = new NotificationCompat.Builder(
                context.getApplicationContext(), AppNotifications.CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setContentTitle("Spy Camera")
                .setContentText("Running in background")
                .setSmallIcon(R.drawable.logo)
                .setAutoCancel(true);
        return notificationBuilder.build();
    }
}
