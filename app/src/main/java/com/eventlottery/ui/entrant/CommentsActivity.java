package com.eventlottery.ui.entrant;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eventlottery.controller.EventController;
import com.eventlottery.databinding.ActivityCommentsBinding;
import com.eventlottery.model.Attendee;
import com.eventlottery.model.Comment;
import com.eventlottery.ui.adapters.CommentAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * CommentsActivity
 *
 * Displays the comments section for a specific event.
 *
 * Features:
 * - View all comments on an event
 * - Post a new comment as an entrant or organizer
 * - Delete any comment if the current user is the organizer
 */
public class CommentsActivity extends AppCompatActivity {

    private static final String TAG = "CommentsActivity";

    private @NonNull ActivityCommentsBinding binding;
    private EventController eventController;

    private String eventId;
    private String organizerId;
    private String currentAttendeeId;
    private String currentAttendeeName;
    private boolean isOrganizer;

    private List<Comment> commentList;
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        eventController = new EventController();

        // Retrieve event and organizer IDs from the launching Intent
        eventId = getIntent().getStringExtra("EVENT_ID");
        organizerId = getIntent().getStringExtra("ORGANIZER_ID");

        setupToolbar();
        setupRecyclerView();

        // Get the current user's Firebase ID asynchronously, then load everything
        Attendee.getFirebaseId().addOnSuccessListener(id -> {
            currentAttendeeId = id;
            isOrganizer = currentAttendeeId.equals(organizerId);
            fetchCurrentAttendeeName();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get Firebase ID", e);
            Toast.makeText(this, "Error identifying user", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Sets up the toolbar with a back navigation button.
     */
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Comments");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }


    private void setupRecyclerView() {
        commentList = new ArrayList<>();
        // Adapter is initialized with placeholder values; rebuilt after identity is confirmed
        commentAdapter = new CommentAdapter(commentList, false, null);
        binding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvComments.setAdapter(commentAdapter);
    }

    /**
     * Fetches the current attendee's name from Firestore so it can be
     * attached to new comments as the author name.
     * After the name is retrieved, loads comments and sets up the send button.
     */
    private void fetchCurrentAttendeeName() {
        Attendee attendee = new Attendee();
        attendee.setID(currentAttendeeId);
        attendee.fetchFromFirebase(new Attendee.OnAttendeeLoadedListener() {
            @Override
            public void onSuccess(Attendee loadedAttendee) {
                currentAttendeeName = loadedAttendee.getName();
                // Now that we have name and role, rebuild adapter and load comments
                rebuildAdapter();
                loadComments();
                setupSendButton();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching attendee name", e);
                // Fall back to a generic name so the user can still comment
                currentAttendeeName = "Anonymous";
                rebuildAdapter();
                loadComments();
                setupSendButton();
            }
        });
    }

    private void rebuildAdapter() {
        commentAdapter = new CommentAdapter(commentList, isOrganizer, this::confirmDelete);
        binding.rvComments.setAdapter(commentAdapter);
    }

    /**
     * Fetches all comments for the event from Firestore and updates the RecyclerView.
     */
    private void loadComments() {
        eventController.getComments(eventId, new EventController.OnCommentsLoadedListener() {
            @Override
            public void onCommentsLoaded(List<Comment> comments) {
                commentList.clear();
                commentList.addAll(comments);
                commentAdapter.notifyDataSetChanged();

                // Scroll to the bottom to show the most recent comment
                if (!commentList.isEmpty()) {
                    binding.rvComments.scrollToPosition(commentList.size() - 1);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading comments", e);
                Toast.makeText(CommentsActivity.this, "Failed to load comments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sets up the send button to post a new comment.
     */
    private void setupSendButton() {
        binding.btnSendComment.setOnClickListener(v -> {
            String text = binding.etCommentInput.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            postComment(text);
        });
    }

    /**
     * Posts a new comment to Firestore.
     * Clears the input field and refreshes the list on success.
     *
     * @param text The comment text to post.
     */
    private void postComment(String text) {
        Comment comment = new Comment(currentAttendeeId, currentAttendeeName, text, isOrganizer);

        eventController.addComment(eventId, comment, new EventController.OnEventOperationListener() {
            @Override
            public void onSuccess() {
                binding.etCommentInput.setText("");
                loadComments();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error posting comment", e);
                Toast.makeText(CommentsActivity.this, "Failed to post comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows a confirmation before deleting a comment
     */
    private void confirmDelete(Comment comment) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Delete", (dialog, which) -> deleteComment(comment))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes a comment from Firestore and refreshes the list
     */
    private void deleteComment(Comment comment) {
        eventController.deleteComment(eventId, comment.getCommentId(), new EventController.OnEventOperationListener() {
            @Override
            public void onSuccess() {
                loadComments();
                Toast.makeText(CommentsActivity.this, "Comment deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error deleting comment", e);
                Toast.makeText(CommentsActivity.this, "Failed to delete comment", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
