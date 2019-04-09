package com.koksheng.procleanservices;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;

public class NotificationHelper extends ContextWrapper {

    private static final String KOKSHENG_CHANNEL_ID = "com.koksheng.procleanservices.KOKSHENG";
    private static final String KOKSHENG_CHANNEL_NAME = "KOKSHENG Channel";
    private NotificationManager manager;
    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationHelper(Context base) {
        super(base);
        createChannels();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {
        NotificationChannel ksChannel = new NotificationChannel(KOKSHENG_CHANNEL_ID,KOKSHENG_CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT);
        ksChannel.enableLights(true);
        ksChannel.enableVibration(true);
        ksChannel.setLightColor(Color.GREEN);
        ksChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(ksChannel);
    }

    public NotificationManager getManager() {
        if (manager == null)
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getKOKSHENGChannelNotification(String title, String body)
    {
        return new Notification.Builder(getApplicationContext(), KOKSHENG_CHANNEL_ID)
                .setContentText(body)
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setAutoCancel(true);
    }
}
