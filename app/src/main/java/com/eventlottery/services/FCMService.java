package com.eventlottery.services;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import androidx.annotation.NonNull;

public class FCMService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Handle incoming push notifications
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Update token on server
    }
}
