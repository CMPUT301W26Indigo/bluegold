package com.eventlottery.ui.entrant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.eventlottery.R;
import com.eventlottery.databinding.ActivityBrowseEventsBinding;
import com.eventlottery.services.LocationService;
import com.eventlottery.ui.qr.QRScannerActivity;

/**
 * Activity hosting the BrowseEventsFragment.
 * Part of the 'View' in MVC, acts as a container for Fragments.
 */
public class BrowseEventsActivity extends AppCompatActivity {
    private LocationService locationService = new LocationService(this);
    private ActivityBrowseEventsBinding binding;
    private double userLat = 0;
    private double userLon = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrowseEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        locationService.requestLocation(new LocationService.LocationCallback() {
            @Override
            public void onLocationReady(double lat, double lon) {
                userLat = lat;
                userLon = lon;
                Log.d("Location", "Latitude: " + lat + ", Longitude: " + lon);

                if (savedInstanceState == null) {
                    BrowseEventsFragment fragment = new BrowseEventsFragment();

                    Bundle args = new Bundle();
                    args.putDouble("userLat", userLat);
                    args.putDouble("userLon", userLon);
                    fragment.setArguments(args);

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                }
            }

            @Override
            public void onPermissionDenied() {
                Log.d("Location", "Permission denied");
            }
        });

        setupBottomNavigation();
    }

    /**
     * Sets up the bottom navigation bar.
     */
    private void setupBottomNavigation() {
        binding.bottomNavigation.setSelectedItemId(R.id.navigation_events);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_events) {
                return true;
            } else if (itemId == R.id.navigation_my_events) {
                startActivity(new Intent(this, MyEventsActivity.class));
                return true;
            } else if (itemId == R.id.navigation_notifications) {
                startActivity(new Intent(this, NotificationsActivity.class));
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.navigation_scan_qr) {
                startActivity(new Intent(this, QRScannerActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
