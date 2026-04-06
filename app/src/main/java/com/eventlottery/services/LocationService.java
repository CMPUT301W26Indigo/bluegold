package com.eventlottery.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

/**
 * Class for getting the user's current location.
 * Prompts the user with a launcher and waits for a result.
 * Then tracks the users location.
 *
 * Coded by Google Gemini, Prompt: "I need the location to be in its own class
 * for several activities to use"
 */
public class LocationService {

    /**
     * Interface for handling the location callback.
     */
    public interface LocationCallback {
        void onLocationReady(double lat, double lon);
        void onPermissionDenied();
    }

    private final ActivityResultLauncher<String> permissionLauncher;
    private LocationCallback currentCallback;
    private final Context context;

    /**
     * Constructor for LocationService to start up.
     * permissionLauncher handles the prompt
     *
     * @param activity The activity to register the launcher with.
     */
    public LocationService(AppCompatActivity activity) {
        this.context = activity;

        // This is the "Modern" way to wait for a result in Android
        this.permissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // User said YES! Now we can safely get the location.
                        startFetchingLocation(context, currentCallback);
                    } else {
                        if (currentCallback != null) currentCallback.onPermissionDenied();
                    }
                }
        );
    }

    /**
     * Requests the user's location through code.
     * @param callback The callback to handle the location.
     */
    public void requestLocation(LocationCallback callback) {
        this.currentCallback = callback;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // We already have it! Go straight to fetching.
            startFetchingLocation(context, callback);
        } else {
            // We don't have it. Launch the popup and wait.
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Starts fetching the user's location and sends off
     * the coordinates
     * @param context The context to use
     * @param listener The callback to handle the location
     */
    private void startFetchingLocation(Context context, LocationCallback listener) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(
                location -> {
                    double latitude = 0;
                    double longitude = 0;
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }

                    if (listener != null) {
                        listener.onLocationReady(latitude, longitude);
                    }
                });
    }

}