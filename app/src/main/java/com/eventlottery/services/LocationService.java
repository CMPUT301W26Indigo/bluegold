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

public class LocationService {

    // 1. This is our "Callback". It tells the Activity when we are done.
    public interface LocationCallback {
        void onLocationReady(double lat, double lon);
        void onPermissionDenied();
    }

    private final ActivityResultLauncher<String> permissionLauncher;
    private LocationCallback currentCallback;
    private final Context context;

    // 2. The Constructor: We set up the "Listener" here
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

    // 3. This is the method you call from your Button click
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

    private void startFetchingLocation(Context context, LocationCallback listener) {
        // Here you call your existing logic (FusedLocationProvider)
        // And when it succeeds, you call currentCallback.onLocationReady(lat, lon);
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
