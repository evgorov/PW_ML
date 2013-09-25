package com.ltst.prizeword.push;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.navigation.NavigationActivity;
import com.ltst.prizeword.rest.RestParams;

import org.omich.velo.log.Log;

import javax.annotation.Nullable;

public class GcmIntentService extends IntentService
{
    public static final int NOTIFICATION_ID = 1;
    private @Nullable NotificationManager mNotificationManager;

    public static final String TAG = "GcmIntentService.class";

    public GcmIntentService()
    {
        super("GcmIntentService");
    }

    public GcmIntentService(String name)
    {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Bundle extras = intent.getExtras();
        if (extras == null)
        {
            return;
        }

        boolean notificationsEnabled = SharedPreferencesValues.getNotificationsSwitch(this);
        if(!notificationsEnabled)
            return;

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                @Nullable String message = extras.getString(RestParams.MESSAGE);
                if (message == null)
                {
                    sendNotification(extras.toString());
                }
                else
                {
                    sendNotification(message);
                }
                Log.i(TAG, "Received notification: " + extras.toString());
            }
        }
        Log.i(TAG, "Completed service @ " + SystemClock.elapsedRealtime());
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NavigationActivity.class), 0);

        Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.push);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Уведомление от PrizeWord")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg)
                        .setSound(sound);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}
