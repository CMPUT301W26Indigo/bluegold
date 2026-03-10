package com.eventlottery.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.eventlottery.databinding.FragmentAdminDashboardBinding;

/**
 * Fragment for the Admin Dashboard.
 * Part of the 'View' in MVC.
 */
public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupNavigation();
    }

    private void setupNavigation() {
        binding.cardManageEvents.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ManageEventsActivity.class));
        });

        binding.cardManageUsers.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ManageUsersActivity.class));
        });

        binding.cardReviewImages.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ReviewImagesActivity.class));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
