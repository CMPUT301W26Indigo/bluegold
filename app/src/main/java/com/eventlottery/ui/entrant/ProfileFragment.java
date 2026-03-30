package com.eventlottery.ui.entrant;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.eventlottery.databinding.FragmentProfileBinding;
import com.eventlottery.model.Attendee;

/**
 * Fragment for the User Profile.
 * Part of the 'View' in MVC, interacting with the Attendee 'Model'.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
    private Attendee currentAttendee;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        currentAttendee = new Attendee();
        
        // Retrieve the unique ID and load the profile
        Attendee.getFirebaseId().addOnSuccessListener(id -> {
            currentAttendee.setAttendeeID(id);
            loadAttendeeProfile();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get Firebase ID", e);
            Toast.makeText(getContext(), "Error identifying device", Toast.LENGTH_SHORT).show();
        });

        setupListeners();
    }

    private void loadAttendeeProfile() {
        currentAttendee.fetchFromFirebase(new Attendee.OnAttendeeLoadedListener() {
            @Override
            public void onSuccess(Attendee attendee) {
                if (isAdded() && binding != null) {
                    binding.etFullName.setText(attendee.getName());
                    binding.etEmail.setText(attendee.getEmail());
                    binding.etPhone.setText(attendee.getPhoneNumber());
                    binding.switchNotifications.setChecked(attendee.getNotification());
                }
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "No existing profile found or error loading: " + e.getMessage());
                // This is fine for new users; the fields will just remain empty.
            }
        });
    }

    private void setupListeners() {
        binding.btnSaveChanges.setOnClickListener(v -> {
            if (currentAttendee != null) {
                try {
                    // Updating the model automatically triggers its saveToFirebase() method
                    currentAttendee.setName(binding.etFullName.getText().toString());
                    currentAttendee.setEmail(binding.etEmail.getText().toString());
                    currentAttendee.setPhoneNumber(binding.etPhone.getText().toString());
                    currentAttendee.setNotification(binding.switchNotifications.isChecked());

                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    
                    // Optional: Close fragment or navigate back
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
