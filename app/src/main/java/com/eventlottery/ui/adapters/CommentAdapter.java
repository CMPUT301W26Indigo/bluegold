package com.eventlottery.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventlottery.R;
import com.eventlottery.model.Comment;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Displays each comment's author name, text, and timestamp.
 * If the current viewer is the organizer, a delete button is shown on each comment.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    /**
     * Listener interface for comment delete actions.
     */
    public interface OnCommentDeleteListener {
        void onDelete(Comment comment);
    }

    private final List<Comment> comments;
    private final boolean isOrganizer;
    private final OnCommentDeleteListener deleteListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());

    /**
     * Constructs CommentAdapter
     */
    public CommentAdapter(List<Comment> comments, boolean isOrganizer, OnCommentDeleteListener deleteListener) {
        this.comments = comments;
        this.isOrganizer = isOrganizer;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);

        // Display author name, appending "(Organizer)" badge if applicable
        String displayName = comment.isOrganizer()
                ? comment.getAuthorName() + " (Organizer)"
                : comment.getAuthorName();
        holder.authorName.setText(displayName);

        holder.commentText.setText(comment.getText());

        // Format and display timestamp if available
        if (comment.getTimestamp() != null) {
            holder.timestamp.setText(dateFormat.format(comment.getTimestamp()));
        } else {
            holder.timestamp.setText("");
        }

        // Show delete button only for the organizer
        if (isOrganizer) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(comment);
                }
            });
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    /**
     * ViewHolder for a single comment item.
     */
    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView authorName;
        TextView commentText;
        TextView timestamp;
        ImageButton deleteButton;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            authorName = itemView.findViewById(R.id.tv_comment_author);
            commentText = itemView.findViewById(R.id.tv_comment_text);
            timestamp = itemView.findViewById(R.id.tv_comment_timestamp);
            deleteButton = itemView.findViewById(R.id.btn_delete_comment);
        }
    }
}
