package com.eventlottery.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Service to handle Firebase Cloud Messaging (FCM) events.
 * Respects user opt-out preference.
 */
public class FCMService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "event_notifications";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Respect opt-out: Fetch attendee preference from Firestore
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(id -> {
            FirebaseFirestore.getInstance().collection("attendees").document(id)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Boolean notificationsEnabled = documentSnapshot.getBoolean("notification");
                        if (notificationsEnabled != null && !notificationsEnabled) {
                            Log.d(TAG, "Push notification suppressed: User has opted out.");
                            return;
                        }

                        // Proceed with notification if enabled or not set
                        handleMessage(remoteMessage);
                    })
                    .addOnFailureListener(e -> {
                        // Fallback: handle message if we can't check preference
                        handleMessage(remoteMessage);
                    });
        });
    }

    private void handleMessage(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            sendNotification(title, body);
        } else if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            sendNotification(title != null ? title : "Event Lottery", body != null ? body : "New update received");
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        updateTokenOnServer(token);
    }

    private void updateTokenOnServer(String token) {
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(id -> {
            FirebaseFirestore.getInstance().collection("attendees").document(id)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM Token updated for attendee: " + id))
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "FCM Token update failed: " + e.getMessage());
                    });
        });
    }

    private void sendNotification(String title, String messageBody) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Event Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}
