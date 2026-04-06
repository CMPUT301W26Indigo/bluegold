package com.eventlottery.ui.organizer;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.eventlottery.databinding.ActivityWaitlistMapBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class WaitlistMapActivity extends AppCompatActivity {
    private ActivityWaitlistMapBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();;

     @Override
    protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         binding = ActivityWaitlistMapBinding.inflate(getLayoutInflater());
         // Set a unique user agent to prevent being blocked by OSM servers
         org.osmdroid.config.Configuration.getInstance().setUserAgentValue(getPackageName());
         org.osmdroid.config.Configuration.getInstance().load(this,
                 androidx.preference.PreferenceManager.getDefaultSharedPreferences(this));

         setContentView(binding.getRoot());


         setSupportActionBar(binding.toolbar);
         if (getSupportActionBar() != null) {
             getSupportActionBar().setDisplayHomeAsUpEnabled(true);
             binding.toolbar.setNavigationOnClickListener(v -> finish());
         }

         String eventId = getIntent().getStringExtra("EVENT_ID");
         double eventLat = getIntent().getDoubleExtra("EVENT_LAT", 0.0);
         double eventLon = getIntent().getDoubleExtra("EVENT_LON", 0.0);

         setupUI(eventLat, eventLon);
         getWaitlist(eventId);
    }

    private void getWaitlist(String eventId) {
        db.collection("events").document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Double lat = document.getDouble("latitude");
                            Double lon = document.getDouble("longitude");

                            if (lat != null && lon != null) {
                                GeoPoint point = new GeoPoint(lat, lon);
                                addMarker(point, "Entrant");
                            }
                        }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error fetching waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addMarker(GeoPoint point, String title) {
        Marker marker = new Marker(binding.waitlistMap);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);
        binding.waitlistMap.getOverlays().add(marker);
        binding.waitlistMap.invalidate(); // Refresh map
    }

    private void setupUI(double eventLat, double eventLon) {
        MapView map = binding.waitlistMap;
        map.setTileSource(TileSourceFactory.MAPNIK); // standard OSM map style
        map.setMultiTouchControls(true); // lets user pinch to zoom

        IMapController mapController = map.getController();

        GeoPoint point = new GeoPoint(eventLat, eventLon);

        // Move the map
        mapController.animateTo(point); // animateTo is smoother than setCenter
        mapController.setZoom(12.0);   // zoom in close

        // Drop a marker
        map.getOverlays().clear();
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Event Location");
        map.getOverlays().add(marker);
        map.invalidate(); // tells the map to redraw
    }

    @Override
    protected void onDestroy() {
         super.onDestroy();
         binding = null;
    }
}

