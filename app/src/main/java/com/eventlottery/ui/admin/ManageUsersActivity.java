package com.eventlottery.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eventlottery.controller.AdminController;
import com.eventlottery.controller.UserController;
import com.eventlottery.databinding.ActivityManageUsersBinding;
import com.eventlottery.model.Attendee;
import com.eventlottery.ui.adapters.UserAdapter;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity implements UserAdapter.OnAttendeeClickListener {

    private static final String TAG = "ManageUsersActivity";
    private ActivityManageUsersBinding binding;
    private UserAdapter adapter;
    private AdminController adminController;
    private UserController userController;
    private List<Attendee> allAttendees = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adminController = new AdminController();
        userController = new UserController();
        setupUI();
        loadUsers();
    }

    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }

        adapter = new UserAdapter(this);
        // We can reuse UserAdapter, but we need to customize it for Admin (e.g. change button text)
        adapter.setInviteButtonText("Delete User");
        
        binding.rvManageUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvManageUsers.setAdapter(adapter);

        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        adminController.getAllAttendees(new AdminController.OnDataLoadedListener<Attendee>() {
            @Override
            public void onDataLoaded(List<Attendee> attendees) {
                allAttendees.clear();
                allAttendees.addAll(attendees);
                adapter.submitList(new ArrayList<>(allAttendees));
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading users", e);
                Toast.makeText(ManageUsersActivity.this, "Error loading users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers(String query) {
        List<Attendee> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Attendee attendee : allAttendees) {
            boolean matchesName = attendee.getName() != null && attendee.getName().toLowerCase().contains(lowerQuery);
            boolean matchesEmail = attendee.getEmail() != null && attendee.getEmail().toLowerCase().contains(lowerQuery);
            boolean matchesPhone = attendee.getPhoneNumber() != null && attendee.getPhoneNumber().toLowerCase().contains(lowerQuery);
            if (matchesName || matchesEmail || matchesPhone) {
                filtered.add(attendee);
            }
        }
        adapter.submitList(filtered);
    }

    @Override
    public void onAttendeeClick(Attendee attendee) {
        // Not used here yet
    }

    @Override
    public void onInviteClick(Attendee attendee) {
        // Using "Invite" button as "Delete" button for admin
        showDeleteConfirmation(attendee);
    }

    @Override
    public void onUninviteClick(Attendee attendee) {
        // Not used here
    }

    private void showDeleteConfirmation(Attendee attendee) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete user: " + attendee.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    showFinalDeleteConfirmation(attendee);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showFinalDeleteConfirmation(Attendee attendee) {
        new AlertDialog.Builder(this)
                .setTitle("Final Confirmation")
                .setMessage("This action is permanent and cannot be undone. Are you REALLY sure you want to delete " + attendee.getName() + "?")
                .setPositiveButton("YES, DELETE", (dialog, which) -> {
                    performDelete(attendee);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void performDelete(Attendee attendee) {
        userController.deleteUser(attendee.getID(), new UserController.OnUserOperationListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(ManageUsersActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                loadUsers(); // Refresh list
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error deleting user", e);
                Toast.makeText(ManageUsersActivity.this, "Error deleting user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
