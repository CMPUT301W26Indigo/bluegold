package com.eventlottery.ui.entrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.eventlottery.controller.UserController;
import com.eventlottery.databinding.FragmentProfileBinding;
import com.eventlottery.model.User;

/**
 * Fragment for the User Profile.
 * Part of the 'View' in MVC.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private UserController userController;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userController = new UserController();
        
        // In a real app, we would get the current user ID from Firebase Auth
        String currentUserId = "mock_user_id";
        loadUserProfile(currentUserId);

        setupListeners();
    }

    private void loadUserProfile(String userId) {
        userController.getUser(userId, new UserController.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                currentUser = user;
                binding.etFullName.setText(user.getName());
                binding.etEmail.setText(user.getEmail());
                binding.etPhone.setText(user.getPhone());
            }

            @Override
            public void onError(Exception e) {
                // If user doesn't exist, create a new one
                currentUser = new User();
                currentUser.setId(userId);
            }
        });
    }

    private void setupListeners() {
        binding.btnSaveChanges.setOnClickListener(v -> {
            if (currentUser != null) {
                currentUser.setName(binding.etFullName.getText().toString());
                currentUser.setEmail(binding.etEmail.getText().toString());
                currentUser.setPhone(binding.etPhone.getText().toString());

                userController.saveUser(currentUser, new UserController.OnUserOperationListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
