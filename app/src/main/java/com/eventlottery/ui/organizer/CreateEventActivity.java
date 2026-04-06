package com.eventlottery.ui.organizer;

import static com.eventlottery.services.Base64EncodeDecode.encodeImageToBase64;

import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.eventlottery.controller.EventController;
import com.eventlottery.databinding.ActivityCreateEventBinding;
import com.eventlottery.model.AbstractUser;
import com.eventlottery.model.Event;
import com.eventlottery.services.ImagePicker;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;


/**
 * Handles event creation with geolocation toggle functionality.
 *
 * This activity allows organizers to enable or disable
 * geolocation requirements for their events. When enabled, organizers can set
 * a radius (1-500km) that entrants must be within to join.
 *
 * User stories implemented:
 * US 02.02.03
 * US 02.01.04
 * US 02.03.01
 *
 * Layout file: activity_create_event.xml
 *
 * Outstanding issues:
 * - Geolocation coordinates are hardcoded
 * - Registration dates are hardcoded
 * - No Organizer ID
 * - Some Number only Textbooks accept letters
 *
 * @see com.eventlottery.model.Event
 * @see com.google.firebase.firestore.FirebaseFirestore
 */
public class CreateEventActivity extends AppCompatActivity {

    private ActivityCreateEventBinding binding;
    private Long registrationOpensTime = 0L;
    private Long registrationClosesTime = 0L;
    private boolean limitWaitlist;
    private boolean eventPrivacy;
    private EventController eventController = new EventController();
    private Uri selectedImageUri;
    private String organizerId;
    private String[] pickedResults;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private ImagePicker imagePicker;




    /**
     * Launches an image picker which allows the user
     * to select an image and then sets the selected image URI.
     * Written by Google Gemini, Prompt: "How would you be able to
     * get the user to browse and input an image?"
     */

    private void uploadImage(Event event, Uri imageUri) {
        if (imageUri != null) {
            String base64Image = encodeImageToBase64(this,imageUri);
            if (base64Image != null) {
                event.setPosterImageUrl(base64Image);
            }
        }
    }

    private List<String[]> searchNominatim(String query) {
        List<String[]> results = new ArrayList<>();
        try{
            String encoded = URLEncoder.encode(query, "UTF-8");
            String url = "https://nominatim.openstreetmap.org/search?q="
                    + encoded
                    + "&format=json&limit=5";
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "com.eventLottery");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONArray json = new JSONArray(response.toString());
            for (int i = 0; i < json.length(); i++) {
                JSONObject obj = json.getJSONObject(i);
                String name = obj.getString("display_name");
                String lat = obj.getString("lat");
                String lon = obj.getString("lon");
                results.add(new String[]{name, lat, lon});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Called when the activity is first created.
     * @param savedInstanceState Saved data when the instance was last closed
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityCreateEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        org.osmdroid.config.Configuration.getInstance().load(this,
                androidx.preference.PreferenceManager.getDefaultSharedPreferences(this));

        AbstractUser.getFirebaseId().addOnSuccessListener(id -> {
            organizerId = id;
        });
        imagePicker = new ImagePicker(this, uri -> {
            selectedImageUri = uri;
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // Show the image in the ImageView
            binding.posterImageView.setImageURI(uri);

            // Remove the grey tint so the actual image shows
            binding.posterImageView.setImageTintList(null);

            binding.posterImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            // Adjust the ImageView to be larger but leave room for the button
            ViewGroup.LayoutParams params = binding.posterImageView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = (int) (130 * getResources().getDisplayMetrics().density);
            binding.posterImageView.setLayoutParams(params);

            // Hide placeholder text but KEEP the button visible
            binding.uploadTitleText.setVisibility(View.GONE);
            binding.uploadSubtitleText.setVisibility(View.GONE);

            // Update the button text so the user knows they can change it
            binding.browseFilesButton.setText("Change Poster");
        });

        /* OrganizerId commented out for now as login not fully implemented
        organizerId = getIntent().getStringExtra("ORGANIZER_ID");
        if (organizerId == null) {
            makeToast()
        } */

        setupUI();

    }

    /**
     * Sets up the UI elements and its functionality to work for creating
     * events.
     */
    private void setupUI() {

        binding.cancelButton.setOnClickListener(v -> finish());

        // Date Picker logic
        //All date and time Picker Logic for Date, Time, and Registration was written by Google Gemini:
        //Prompt: "How would users select a date and time without directly entering it as a String?"
        binding.eventDateEditText.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Event Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                binding.eventDateEditText.setText(datePicker.getHeaderText());
            });

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        // Time Picker logic
        binding.eventTimeEditText.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select Event Time")
                    .build();

            timePicker.addOnPositiveButtonClickListener(view -> {
                binding.eventTimeEditText.setText(String.format("%02d:%02d", timePicker.getHour(), timePicker.getMinute()));
            });

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
        });
        
        // Registration Opens Picker logic
        binding.registrationOpensEditText.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Registration Open Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                registrationOpensTime = selection;
                binding.registrationOpensEditText.setText(datePicker.getHeaderText());
            });
            datePicker.show(getSupportFragmentManager(), "REG_OPEN_PICKER");
        });

        // Registration Closes Picker logic
        binding.registrationClosesEditText.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Registration Close Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                registrationClosesTime = selection;
                binding.registrationClosesEditText.setText(datePicker.getHeaderText());
            });
            datePicker.show(getSupportFragmentManager(), "REG_CLOSE_PICKER");
        });

        //gives functionality to the limit switches
        binding.waitlistLimitSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.waitlistLimitEditText.setEnabled(isChecked);
            binding.waitlistLimitLayout.setEnabled(isChecked);
        });

        binding.geolocationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.radiusEditText.setEnabled(isChecked);
            binding.radiusLayout.setEnabled(isChecked);
        });

        binding.privacySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.radiusEditText.setEnabled(isChecked);
            binding.radiusLayout.setEnabled(isChecked);
        });

        //specifies to only browse images
        binding.browseFilesButton.setOnClickListener(v -> {
            imagePicker.pickImage();
        });

        binding.locationSearchButton.setOnClickListener(v -> {
            String query = binding.locationEditText.getText().toString();
            new Thread(() -> {
                List<String[]> results = searchNominatim(query);
                runOnUiThread(() -> {
                    if (results.size() > 0) {
                        List<String> names = new ArrayList<>();
                        for (String[] result : results) {
                            names.add(result[0]);
                        }

                        MapView map = binding.createEventMap;
                        map.setVisibility(View.GONE);
                        map.setTileSource(TileSourceFactory.MAPNIK); // standard OSM map style
                        map.setMultiTouchControls(true); // lets user pinch to zoom

                        IMapController mapController = map.getController();
                        mapController.setZoom(12.0); // default zoom level

                        ArrayAdapter<String> dropDownAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
                        binding.locationResultsList.setAdapter(dropDownAdapter);
                        binding.locationResultsList.setVisibility(View.VISIBLE);
                        binding.locationResultsList.setNestedScrollingEnabled(true);

                        binding.locationResultsList.setOnItemClickListener((parent, view, position, id) -> {
                            binding.locationEditText.setText(names.get(position));
                            binding.locationResultsList.setVisibility(View.GONE);
                            pickedResults = results.get(position);
                            Log.d("Nominatim", Arrays.toString(pickedResults));

                            GeoPoint point = new GeoPoint(Double.parseDouble(pickedResults[1]), Double.parseDouble(pickedResults[2]));

                            // Move the map
                            mapController.animateTo(point); // animateTo is smoother than setCenter
                            mapController.setZoom(16.0);   // zoom in close

                            // Drop a marker
                            map.getOverlays().clear();
                            Marker marker = new Marker(map);
                            marker.setPosition(point);
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            marker.setTitle("Event Location");
                            map.getOverlays().add(marker);
                            map.invalidate(); // tells the map to redraw
                            map.setVisibility(View.VISIBLE);
                        });
                    } else {
                        Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();

                    }
                });
            }).start();
        });



        //beginning to create the event and assign its details and push it to the database
        binding.createEventButton.setOnClickListener(v -> {
            if (!validateInput()) {
                return;
            }

            Event event = new Event();
            event.setName(binding.eventNameEditText.getText().toString());
            event.setDescription(binding.descriptionEditText.getText().toString());
            event.setDate(binding.eventDateEditText.getText().toString());
            event.setTime(binding.eventTimeEditText.getText().toString());

            // Setting the actual timestamps captured from the pickers
            event.setRegistrationOpens(registrationOpensTime);
            event.setRegistrationCloses(registrationClosesTime);
            
            AbstractUser.getFirebaseId().addOnSuccessListener(id -> {
                Log.e("EventController", "Organizer ID: " + id);
                organizerId = id;
                // Set the captured organizer ID
                event.setOrganizerId(organizerId);

                try {
                    event.setCapacity(Integer.parseInt(binding.capacityEditText.getText().toString()));
                } catch (NumberFormatException e) {
                    event.setCapacity(0);
                }

                // Set waitlist limits if they exist
                limitWaitlist = binding.waitlistLimitSwitch.isChecked();
                event.setWaitlistLimit(limitWaitlist ? 1 : 0);
                if (limitWaitlist) {
                    try {
                        event.setWaitlistLimit(Integer.parseInt(binding.waitlistLimitEditText.getText().toString()));
                    } catch (NumberFormatException e) {
                        event.setWaitlistLimit(null);
                    }
                }

                // Set event privacy if it exists
                eventPrivacy = binding.privacySwitch.isChecked();
                event.setPrivate(eventPrivacy);

                // Set the location and geolocation
                event.setLocation(binding.locationEditText.getText().toString());
                event.setLatitude(Double.parseDouble(pickedResults[1]));
                event.setLongitude(Double.parseDouble(pickedResults[2]));
                event.setGeolocationEnabled(binding.geolocationSwitch.isChecked());
                if (binding.geolocationSwitch.isChecked()) {
                    try {
                        event.setGeolocationRadius(Integer.valueOf(binding.radiusEditText.getText().toString()));
                    } catch (NumberFormatException e) {
                        event.setGeolocationRadius(null);
                    }
                }

                // Set the tags for an event
                List<String> selectedTags = new ArrayList<>();
                for (Integer chipId : binding.tagChipGroup.getCheckedChipIds()) {
                    Chip chip = binding.tagChipGroup.findViewById(chipId);
                    selectedTags.add(chip.getText().toString());
                }
                event.setTags(selectedTags);

            // Upload the image
            uploadImage(event, selectedImageUri);

                // Set the price
                try {
                    event.setPrice(Double.parseDouble(binding.priceEditText.getText().toString()));
                } catch (NumberFormatException e) {
                    event.setPrice(0.0);
                }

                //adding the event to the database
                eventController.addEvent(event, new EventController.OnEventOperationListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(CreateEventActivity.this, "Event created successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(CreateEventActivity.this, "Error creating event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    /**
     * Ensures all events have a name, description, date, time, registration period, tags and capacity.
     * @return
     */
    private boolean validateInput() {
        boolean isValid = true;

        if (TextUtils.isEmpty(binding.eventNameEditText.getText())) {
            binding.eventNameLayout.setError("Event name is required");
            isValid = false;
        } else {
            binding.eventNameLayout.setError(null);
        }

        if (TextUtils.isEmpty(binding.descriptionEditText.getText())) {
            binding.descriptionLayout.setError("Description is required");
            isValid = false;
        } else {
            binding.descriptionLayout.setError(null);
        }

        if (TextUtils.isEmpty(binding.eventDateEditText.getText())) {
            binding.eventDateLayout.setError("Date is required");
            isValid = false;
        } else {
            binding.eventDateLayout.setError(null);
        }

        if (TextUtils.isEmpty(binding.eventTimeEditText.getText())) {
            binding.eventTimeLayout.setError("Time is required");
            isValid = false;
        } else {
            binding.eventTimeLayout.setError(null);
        }

        if (registrationOpensTime == 0L) {
            binding.registrationOpensLayout.setError("Opening date is required");
            isValid = false;
        } else {
            binding.registrationOpensLayout.setError(null);
        }

        if (registrationClosesTime == 0L) {
            binding.registrationClosesLayout.setError("Closing date is required");
            isValid = false;
        } else {
            binding.registrationClosesLayout.setError(null);
        }

        if (TextUtils.isEmpty(binding.capacityEditText.getText())) {
            binding.capacityLayout.setError("Capacity is required");
            isValid = false;
        } else {
            binding.capacityLayout.setError(null);
        }

        if (pickedResults == null) {
            binding.locationLayout.setError("Location selection is required");
            isValid = false;
        } else {
            binding.locationLayout.setError(null);
        }

        if (binding.tagChipGroup.getCheckedChipIds().isEmpty()) {
            Toast.makeText(this, "Please select at least one tag", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (!isValid) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }

    /**
     * Destroys the activity and sets the binding to null
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
