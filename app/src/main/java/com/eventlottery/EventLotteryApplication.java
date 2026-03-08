package com.eventlottery;

import android.app.Application;
import com.google.firebase.FirebaseApp;

/**
 * Custom Application class for the Event Lottery System.
 * Used for global initialization.
 */
public class EventLotteryApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
    }
}
