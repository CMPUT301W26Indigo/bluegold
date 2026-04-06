package com.eventlottery.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.eventlottery.MainActivity;
import com.eventlottery.model.Attendee;
import com.eventlottery.model.Notification;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

/**
 * Foreground Service that listens to Firestore for new notifications.
 * Respects user opt-out preference.
 */
public class NotificationService extends Service {
    private static final String TAG = "NotificationService";
    private static final String CHANNEL_ID_SILENT = "service_status_channel";
    private static final String CHANNEL_ID_ALERTS = "foreground_notifications";
    private static final int SERVICE_ID = 1001;
    
    private FirebaseFirestore db;
    private ListenerRegistration notificationListener;
    private ListenerRegistration preferenceListener;
    private String attendeeId;
    private boolean isOptedOut = false;

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
        createNotificationChannels();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start as foreground using the SILENT channel to avoid annoying popups on navigation
        startForeground(SERVICE_ID, createForegroundStatusNotification("Monitoring notifications..."));

        Attendee.getFirebaseId().addOnSuccessListener(id -> {
            this.attendeeId = id;
            startPreferenceListener();
        });

        return START_STICKY;
    }

    /**
     * Listens to the attendee's profile for notification preference changes.
     */
    private void startPreferenceListener() {
        if (preferenceListener != null) preferenceListener.remove();

        preferenceListener = db.collection("attendees").document(attendeeId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) return;
                    if (snapshot != null && snapshot.exists()) {
                        Boolean enabled = snapshot.getBoolean("notification");
                        // Default to true if not set
                        boolean notificationsEnabled = (enabled == null || enabled);
                        
                        if (notificationsEnabled) {
                            isOptedOut = false;
                            startFirestoreListener();
                        } else {
                            isOptedOut = true;
                            if (notificationListener != null) {
                                notificationListener.remove();
                                notificationListener = null;
                            }
                        }
                    }
                });
    }

    private void startFirestoreListener() {
        if (notificationListener != null || isOptedOut) return;

        // Standardized on "read" field to match initial creation
        Query query = db.collection("notifications")
                .whereEqualTo("attendeeId", attendeeId)
                .whereEqualTo("read", false);

        // Flag to ignore existing notifications when the listener first connects
        final boolean[] isInitialSnapshot = {true};

        notificationListener = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Listen failed.", error);
                return;
            }

            if (value != null) {
                // Ignore the initial dump of old unread notifications
                if (isInitialSnapshot[0]) {
                    isInitialSnapshot[0] = false;
                    return;
                }

                for (DocumentChange dc : value.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        Notification notification = dc.getDocument().toObject(Notification.class);
                        showPopupNotification(notification);
                    }
                }
            }
        });
    }

    private void showPopupNotification(Notification n) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Use the ALERTS channel for high-priority popups
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_ALERTS)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(n.getTitle() != null ? n.getTitle() : "Event Update")
                .setContentText(n.getMessage())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private android.app.Notification createForegroundStatusNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID_SILENT)
                .setContentTitle("Event Lottery")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            // 1. Silent channel for service status (no popup)
            NotificationChannel silentChannel = new NotificationChannel(
                    CHANNEL_ID_SILENT,
                    "Service Status",
                    NotificationManager.IMPORTANCE_LOW
            );
            silentChannel.setShowBadge(false);
            manager.createNotificationChannel(silentChannel);

            // 2. High importance channel for actual alerts (popup enabled)
            NotificationChannel alertChannel = new NotificationChannel(
                    CHANNEL_ID_ALERTS,
                    "Event Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(alertChannel);
        }
    }

    @Override
    public void onDestroy() {
        if (notificationListener != null) notificationListener.remove();
        if (preferenceListener != null) preferenceListener.remove();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
