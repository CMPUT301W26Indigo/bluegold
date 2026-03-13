package com.eventlottery.ui.organizer;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;


import com.eventlottery.controller.EventController;
import com.eventlottery.model.EventTemp;
import com.eventlottery.databinding.ActivityCreateEventBinding;

import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;
import java.util.List;


/**
 * Handles event creation with geolocation toggle functionality.
 *
 * This activity allows organizers to enable or disable
 * geolocation requirements for their events. When enabled, organizers can set
 * a radius (1-500km) that entrants must be within to join.
 *
 * User stories implemented:
 * 02.02.03
 *
 * Layout file: activity_create_event.xml
 *
 * Outstanding issues:
 * - Geolocation coordinates are hardcoded
 * - Registration dates are hardcoded
 *
 * @see Event
 * @see com.google.firebase.firestore.FirebaseFirestore
 */
public class CreateEventActivity extends AppCompatActivity {

    private ActivityCreateEventBinding binding;
    private Long registrationOpensTime = 0L;
    private Long registrationClosesTime = 0L;
    private boolean limitWaitlist;
    private EventController eventController = new EventController();
    private Uri selectedImageUri;
    private String organizerId;



    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    
                    // Show the image in the ImageView
                    binding.posterImageView.setImageURI(uri);
                    
                    // Make the ImageView fill the card
                    ViewGroup.LayoutParams params = binding.posterImageView.getLayoutParams();
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    binding.posterImageView.setLayoutParams(params);
                    binding.posterImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    
                    // Hide the placeholder text and button
                    binding.uploadTitleText.setVisibility(View.GONE);
                    binding.uploadSubtitleText.setVisibility(View.GONE);
                    binding.browseFilesButton.setVisibility(View.GONE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityCreateEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /* Get the organizer ID from the intent
        organizerId = getIntent().getStringExtra("ORGANIZER_ID");
        if (organizerId == null) {
            // Fallback for testing/debugging
            organizerId = "test_organizer_id";
        } */

        setupUI();

    }

    private void setupUI() {
        binding.cancelButton.setOnClickListener(v -> finish());

        // Date Picker logic
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

        binding.waitlistLimitSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.waitlistLimitEditText.setEnabled(isChecked);
            binding.waitlistLimitLayout.setEnabled(isChecked);
        });

        binding.geolocationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.radiusEditText.setEnabled(isChecked);
            binding.radiusLayout.setEnabled(isChecked);
        });


        binding.browseFilesButton.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });


        binding.createEventButton.setOnClickListener(v -> {
            EventTemp event = new EventTemp();
            event.setName(binding.eventNameEditText.getText().toString());
            event.setDescription(binding.descriptionEditText.getText().toString());
            event.setDate(binding.eventDateEditText.getText().toString());
            event.setTime(binding.eventTimeEditText.getText().toString());
            
            // Set the captured organizer ID
            //event.setOrganizerId(organizerId);
            
            try {
                if (selectedImageUri != null) {
                    event.setPosterImageUrl(selectedImageUri.toString());
                }
            } catch (Exception e) {
                event.setPosterImageUrl(null);
            }

            // Setting the actual timestamps captured from the pickers
            event.setRegistrationOpens(registrationOpensTime);
            event.setRegistrationCloses(registrationClosesTime);
            
            try {
                event.setCapacity(Integer.parseInt(binding.capacityEditText.getText().toString()));
            } catch (NumberFormatException e) {
                event.setCapacity(0);
            }

            limitWaitlist = binding.waitlistLimitSwitch.isChecked();
            event.setWaitlistLimit(limitWaitlist ? 1 : 0);
            if (limitWaitlist) {
                try {
                    event.setWaitlistLimit(Integer.parseInt(binding.waitlistLimitEditText.getText().toString()));
                } catch (NumberFormatException e) {
                    event.setWaitlistLimit(null);
                }
            }

            
            event.setLocation(binding.locationEditText.getText().toString());
            event.setGeolocationEnabled(binding.geolocationSwitch.isChecked());
            if (binding.geolocationSwitch.isChecked()) {
                event.setGeolocationRadius(Integer.valueOf(binding.radiusEditText.getText().toString()));
            }

            try {
                event.setPrice(Double.parseDouble(binding.priceEditText.getText().toString()));
            } catch (NumberFormatException e) {
                event.setPrice(0.0);
            }

            List<String> selectedTags = new ArrayList<>();
            for (Integer id : binding.tagChipGroup.getCheckedChipIds()) {
                Chip chip = binding.tagChipGroup.findViewById(id);
                selectedTags.add(chip.getText().toString());
            }
            event.setTags(selectedTags);
            
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
