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
import com.eventlottery.databinding.ActivityCreateEventBinding;
import com.eventlottery.model.EventTemp;
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
 * US 02.02.03
 * US 02.01.04
 * US 02.03.01
 *
 * Layout file: activity_create_event.xml
 *
 * Outstanding issues:
 * - Geolocation coordinates are hardcoded
 * - Registration dates are hardcoded
 * - No Organizer Id
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
    private EventController eventController = new EventController();
    private Uri selectedImageUri;
    private String organizerId;


    /**
     * Launches an image picker which allows the user
     * to select an image and then sets the selected image URI.
     * Written by Google Gemini, Prompt: "How would you be able to
     * get the user to browse and input an image?"
     */
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

        /* OrganizerId commented out for now as login not fully implemented
        organizerId = getIntent().getStringExtra("ORGANIZER_ID");
        if (organizerId == null) {
            organizerId = "test_organizer_id";
        } */

        setupUI();

    }

    /**
     * Sets up the UI elements and its functionality to work for creating
     * events.
     */
    private void setupUI() {
        // some setup logic here

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

        //specifies to only browse images
        binding.browseFilesButton.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });


        //beginning to create the event and assign its details and push it to the database
        binding.createEventButton.setOnClickListener(v -> {
            EventTemp event = new EventTemp();
            event.setName(binding.eventNameEditText.getText().toString());
            event.setDescription(binding.descriptionEditText.getText().toString());
            event.setDate(binding.eventDateEditText.getText().toString());
            event.setTime(binding.eventTimeEditText.getText().toString());
            
            // Set the captured organizer ID
            //event.setOrganizerId(organizerId);

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

            List<String> selectedTags = new ArrayList<>();
            for (Integer id : binding.tagChipGroup.getCheckedChipIds()) {
                Chip chip = binding.tagChipGroup.findViewById(id);
                selectedTags.add(chip.getText().toString());
            }
            event.setTags(selectedTags);

            try {
                if (selectedImageUri != null) {
                    event.setPosterImageUrl(selectedImageUri.toString());
                }
            } catch (Exception e) {
                event.setPosterImageUrl(null);
            }

            try {
                event.setPrice(Double.parseDouble(binding.priceEditText.getText().toString()));
            } catch (NumberFormatException e) {
                event.setPrice(0.0);
            }

            //adding the event to the database
            //First generate QRs
            eventController.addEvent(event, new EventController.OnEventOperationListener() {
                @Override
                public void onSuccess() {
                    // Generate the QR code if the event is successful
                    event.setQrCodeUrl("eventlottery://event/" + event.getId());
                    // Are we using event or event temp????
                    event.setQrCode(event.generateQRBitmap(event.getQrCodeUrl()));
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

    /**
     * Destroys the activity and sets the binding to null
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
