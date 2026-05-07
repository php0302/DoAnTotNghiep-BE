package com.example.project_management.feature.comment;

import com.example.project_management.feature.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
    name = "comment_mentions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "user_id"})
)
public class CommentMention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User mentionedUser;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    public CommentMention() {}

    public CommentMention(Comment comment, User mentionedUser) {
        this.comment = comment;
        this.mentionedUser = mentionedUser;
    }

    public Long getId() { return id; }

    public Comment getComment() { return comment; }
    public void setComment(Comment comment) { this.comment = comment; }

    public User getMentionedUser() { return mentionedUser; }
    public void setMentionedUser(User mentionedUser) { this.mentionedUser = mentionedUser; }

    public Instant getCreatedAt() { return createdAt; }
}
