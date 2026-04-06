package com.eventlottery.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eventlottery.controller.AdminController;
import com.eventlottery.databinding.ActivityAdminEventCommentsBinding;
import com.eventlottery.model.Comment;
import com.eventlottery.ui.adapters.CommentAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminEventCommentsActivity
 *
 * Displays all comments for a specific event for administrator review.
 * The admin can delete any comment that violates app policy (US 03.10.01).
 *
 * Reuses CommentAdapter with isOrganizer = true so delete buttons are
 * always visible for every comment, regardless of who posted it.
 *
 * Expected Intent extras:
 *   - "EVENT_ID"   : String - the Firestore document ID of the event
 *   - "EVENT_NAME" : String - the event name shown in the toolbar title
 */
public class AdminEventCommentsActivity extends AppCompatActivity {

    private static final String TAG = "AdminEventCommentsActivity";

    private ActivityAdminEventCommentsBinding binding;
    private AdminController adminController;

    private String eventId;
    private List<Comment> commentList;
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminEventCommentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adminController = new AdminController();

        eventId = getIntent().getStringExtra("EVENT_ID");
        String eventName = getIntent().getStringExtra("EVENT_NAME");

        setupUI(eventName);
        setupRecyclerView();
        loadComments();
    }

    /**
     * Sets up the toolbar with the event name as the title and a back button.
     *
     * @param eventName The name of the event shown in the toolbar.
     */
    private void setupUI(String eventName) {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(eventName != null ? eventName + " — Comments" : "Comments");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Initializes the RecyclerView with an empty adapter.
     * Passes isOrganizer = true so the delete button is visible on every comment.
     */
    private void setupRecyclerView() {
        commentList = new ArrayList<>();
        // Admin can always delete any comment, so isOrganizer = true is passed
        commentAdapter = new CommentAdapter(commentList, true, this::confirmDelete);
        binding.rvAdminComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminComments.setAdapter(commentAdapter);
    }

    /**
     * Fetches all comments for the event from Firestore and updates the RecyclerView.
     */
    private void loadComments() {
        adminController.getComments(eventId, new AdminController.OnDataLoadedListener<Comment>() {
            @Override
            public void onDataLoaded(List<Comment> comments) {
                commentList.clear();
                commentList.addAll(comments);
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading comments", e);
                Toast.makeText(AdminEventCommentsActivity.this, "Failed to load comments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows a confirmation dialog before deleting a comment.
     *
     * @param comment The comment to delete.
     */
    private void confirmDelete(Comment comment) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Comment")
                .setMessage("Are you sure you want to remove this comment for violating app policy?")
                .setPositiveButton("Remove", (dialog, which) -> deleteComment(comment))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes a comment from Firestore and refreshes the list on success
     *
     * @param comment The comment to delete.
     */
    private void deleteComment(Comment comment) {
        adminController.deleteComment(eventId, comment.getCommentId(), new AdminController.OnOperationListener() {
            @Override
            public void onSuccess() {
                loadComments();
                Toast.makeText(AdminEventCommentsActivity.this, "Comment removed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error removing comment", e);
                Toast.makeText(AdminEventCommentsActivity.this, "Failed to remove comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
