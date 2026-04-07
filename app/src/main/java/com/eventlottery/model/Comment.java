package com.eventlottery.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Represents a comment posted on an event.
 * Can be posted by an entrant or the event organizer.
 */
public class Comment {
    private String commentId;
    private String authorId;
    private String authorName;
    private String text;
    private boolean isOrganizer;

    @ServerTimestamp
    private Date timestamp;

    /**
     * Default no-argument constructor required for Firebase Firestore deserialization.
     */
    public Comment() {
    }

    /**
     * Constructs a Comment
     *
     * @param authorId    The ID of the user posting the comment
     * @param authorName  The display name of the user posting the comment
     * @param text        The comment itself
     * @param isOrganizer True if the comment is posted by the event organizer
     */
    public Comment(String authorId, String authorName, String text, boolean isOrganizer) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.text = text;
        this.isOrganizer = isOrganizer;
    }

    // Getters and Setters

    /**
     * Gets the unique ID of the comment.
     * @return The comment ID string.
     */
    public String getCommentId() { return commentId; }

    /**
     * Sets the unique ID of the comment.
     * @param commentId The comment ID to set.
     */
    public void setCommentId(String commentId) { this.commentId = commentId; }

    /**
     * Gets the ID of the user who authored the comment.
     * @return The author's user ID.
     */
    public String getAuthorId() { return authorId; }

    /**
     * Sets the ID of the user who authored the comment.
     * @param authorId The author's user ID to set.
     */
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    /**
     * Gets the display name of the user who authored the comment.
     * @return The author's display name.
     */
    public String getAuthorName() { return authorName; }

    /**
     * Sets the display name of the user who authored the comment.
     * @param authorName The author's display name to set.
     */
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    /**
     * Gets the text content of the comment.
     * @return The comment text.
     */
    public String getText() { return text; }

    /**
     * Sets the text content of the comment.
     * @param text The comment text to set.
     */
    public void setText(String text) { this.text = text; }

    /**
     * Checks if the author of the comment is the event organizer.
     * @return true if authored by the organizer, false otherwise.
     */
    public boolean isOrganizer() { return isOrganizer; }

    /**
     * Sets whether the author of the comment is the event organizer.
     * @param organizer true if authored by the organizer, false otherwise.
     */
    public void setOrganizer(boolean organizer) { isOrganizer = organizer; }

    /**
     * Gets the timestamp when the comment was posted.
     * @return The post timestamp.
     */
    public Date getTimestamp() { return timestamp; }

    /**
     * Sets the timestamp when the comment was posted.
     * @param timestamp The post timestamp to set.
     */
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
