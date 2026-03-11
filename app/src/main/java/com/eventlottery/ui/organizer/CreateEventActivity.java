package com.eventlottery.ui.organizer;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.eventlottery.databinding.ActivityCreateEventBinding;
import com.eventlottery.model.Event;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import java.util.ArrayList;
import java.util.List;

public class CreateEventActivity extends AppCompatActivity {

    private ActivityCreateEventBinding binding;
    private Long registrationOpensTime = 0L;
    private Long registrationClosesTime = 0L;


    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    // The 'uri' is the location of the image on the phone
                    // You can show it in an ImageView or save it to your Event object
                    binding.uploadPosterCard.setBackground(null); // Clear the upload icon
                    // Use a library like Glide or just setImageURI if you had an ImageView
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityCreateEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                registrationClosesTime = selection;
                binding.registrationClosesEditText.setText(datePicker.getHeaderText());
            });
            datePicker.show(getSupportFragmentManager(), "REG_CLOSE_PICKER");
        });

        binding.waitlistLimitSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.waitlistLimitEditText.setEnabled(true);
                binding.waitlistLimitLayout.setEnabled(true);
            } else {
                binding.waitlistLimitEditText.setEnabled(false);
                binding.waitlistLimitLayout.setEnabled(false);
            }
        });


        binding.browseFilesButton.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        binding.createEventButton.setOnClickListener(v -> {
            Event event = new Event();
            event.setName(binding.eventNameEditText.getText().toString());
            event.setDescription(binding.descriptionEditText.getText().toString());
            event.setDate(binding.eventDateEditText.getText().toString());
            event.setTime(binding.eventTimeEditText.getText().toString());
            
            // Setting the actual timestamps captured from the pickers
            event.setRegistrationOpens(registrationOpensTime);
            event.setRegistrationCloses(registrationClosesTime);
            
            try {
                event.setCapacity(Integer.parseInt(binding.capacityEditText.getText().toString()));
            } catch (NumberFormatException e) {
                event.setCapacity(0);
            }
            
            try {
                event.setWaitlistLimit(Integer.parseInt(binding.waitlistLimitEditText.getText().toString()));
            } catch (NumberFormatException e) {
                event.setWaitlistLimit(null);
            }
            
            event.setLocation(binding.locationEditText.getText().toString());
            
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
            
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
