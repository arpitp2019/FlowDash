package com.flowdash.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "mindvault_subject")
public class MindVaultSubject extends AuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private Integer priority = 3;

    @Column(name = "target_mastery", nullable = false)
    private Integer targetMastery = 80;

    @Column(name = "deadline")
    private LocalDate deadline;

    private String tags;

    @Column(nullable = false)
    private boolean archived = false;

    protected MindVaultSubject() {
    }

    public MindVaultSubject(AppUser user, String title, String description, Integer priority, Integer targetMastery, LocalDate deadline, String tags, boolean archived) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.priority = priority == null ? 3 : priority;
        this.targetMastery = targetMastery == null ? 80 : targetMastery;
        this.deadline = deadline;
        this.tags = tags;
        this.archived = archived;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getTargetMastery() {
        return targetMastery;
    }

    public void setTargetMastery(Integer targetMastery) {
        this.targetMastery = targetMastery;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
