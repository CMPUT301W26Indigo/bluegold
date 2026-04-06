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

    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isOrganizer() { return isOrganizer; }
    public void setOrganizer(boolean organizer) { isOrganizer = organizer; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
