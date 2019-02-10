package com.scubearena.testapp;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;


import java.sql.Timestamp;


public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {


    private static final String TAG = FirebaseMessagingService.class.getSimpleName();

    private NotificationUtils notificationUtils;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        System.out.println("********** Entered into onMessageReceived()****************");

        Log.e(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage == null)
            return;

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            System.out.println("********** Entered into onMessageReceived()-getNotification()****************");
            Log.e(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification().getBody());
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            System.out.println("********** Entered into onMessageReceived()-getData()****************");

            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {
                handleDataMessage(remoteMessage);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleNotification(String message) {
        System.out.println("********** Entered into handleNotification()****************");

        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent("pushNotification");
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound("");
        }else{
            // If the app is in background, firebase itself handles the notification
        }
    }

    private void handleDataMessage(RemoteMessage remoteMessage) {
        System.out.println("********** Entered into handleDataMessage()****************");

        try {
            String imageUrl="";
            String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("body");
            String notfTone = remoteMessage.getData().get("tone");
            String clickAction = remoteMessage.getData().get("click_action");
            String toId = remoteMessage.getData().get("id");


            System.out.println("title: " + title);
            System.out.println("message: " + message);
            System.out.println("imageUrl: " + imageUrl);
            System.out.println("timestamp: " + timestamp);
            System.out.println("notfTone: " + notfTone);
            System.out.println("clickAction: " + clickAction);
            System.out.println("toId: " + toId);




            if (!NotificationUtils.isAppIsInBackground(getApplicationContext()))
            {
                System.out.println("********** Entered into foregroundpart()****************");

                // app is in foreground, broadcast the push message
                Intent pushNotification = new Intent(clickAction);
                pushNotification.putExtra("message", message);
                pushNotification.putExtra("user_id",toId);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                // play notification sound
                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                notificationUtils.playNotificationSound(notfTone);
            } else {
                System.out.println("********** Entered into backgroundpart()****************");

                // app is in background, show the notification in notification tray
                Intent resultIntent = new Intent(clickAction);
                resultIntent.putExtra("message", message);
                resultIntent.putExtra("user_id",toId);


                // check for image attachment
                if (!message.contains("https://firebasestorage.googleapis.com/v0/b/testapp-99001.appspot.com/")) {
                    System.out.println("********** Entered into textnotification part()****************");
                    showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent,notfTone);
                } else {
                    // image is present, show notification with image
                    imageUrl=message;
                    message="";
                    showNotificationMessageWithBigImage(getApplicationContext(), title, message, timestamp, resultIntent, imageUrl,notfTone);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent,String tone) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        System.out.println("********** Entered into showNotificationPart()****************");
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent,tone);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl,String tone) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl,tone);
    }
    }
