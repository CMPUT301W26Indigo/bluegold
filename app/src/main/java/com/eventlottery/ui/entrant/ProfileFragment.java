package com.eventlottery.ui.entrant;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.eventlottery.controller.UserController;
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
    private UserController userController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize the controller to avoid NullPointerException
        userController = new UserController();
        currentAttendee = new Attendee();
        
        // Retrieve the unique ID and load the profile
        Attendee.getFirebaseId().addOnSuccessListener(id -> {
            currentAttendee.setID(id);
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

        binding.btnDeleteProfile.setOnClickListener(v -> {
            if (currentAttendee != null) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Profile")
                        .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            userController.deleteUser(currentAttendee.getID(), new UserController.OnUserOperationListener() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(getContext(), "Profile deleted", Toast.LENGTH_SHORT).show();
                                    if (getActivity() != null) {
                                        getActivity().finish();
                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                    Toast.makeText(getContext(), "Failed to delete profile", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
