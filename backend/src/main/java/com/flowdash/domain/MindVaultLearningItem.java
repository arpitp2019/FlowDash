package com.flowdash.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mindvault_learning_item")
public class MindVaultLearningItem extends AuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private MindVaultSubject subject;

    @ManyToOne
    @JoinColumn(name = "sprint_id")
    private MindVaultSprint sprint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MindVaultItemSource source = MindVaultItemSource.PLANNED;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_type", nullable = false)
    private MindVaultLearningType learningType = MindVaultLearningType.IMPORTANT_TOPIC;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String prompt;

    @Column(columnDefinition = "text")
    private String answer;

    @Column(columnDefinition = "text")
    private String notes;

    private String tags;

    @Column(nullable = false)
    private Integer priority = 3;

    @Column(nullable = false)
    private Integer importance = 3;

    @Column(nullable = false)
    private Integer difficulty = 3;

    @Column(name = "mastery_score", nullable = false)
    private Integer masteryScore = 15;

    @Column(name = "review_streak", nullable = false)
    private Integer reviewStreak = 0;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Column(name = "success_count", nullable = false)
    private Integer successCount = 0;

    @Column(name = "lapse_count", nullable = false)
    private Integer lapseCount = 0;

    @Column(name = "ease_factor", nullable = false)
    private Double easeFactor = 2.1d;

    @Column(name = "review_interval_days", nullable = false)
    private Integer reviewIntervalDays = 1;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @Column(name = "last_rating")
    private Integer lastRating;

    @Column(name = "review_enabled", nullable = false)
    private boolean reviewEnabled = true;

    @Column(name = "source_label")
    private String sourceLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MindVaultItemStatus status = MindVaultItemStatus.ACTIVE;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MindVaultReviewLog> reviewLogs = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<MindVaultResource> resources = new ArrayList<>();

    protected MindVaultLearningItem() {
    }

    public MindVaultLearningItem(AppUser user, MindVaultSubject subject, MindVaultSprint sprint, MindVaultItemSource source, String title, String prompt, String answer, String notes, String tags, Integer priority, Integer difficulty, Integer masteryScore, Integer reviewStreak, Integer reviewCount, Integer successCount, Double easeFactor, Integer reviewIntervalDays, LocalDate nextReviewDate, Instant lastReviewedAt, Integer lastRating, MindVaultItemStatus status, LocalDate dueDate) {
        this.user = user;
        this.subject = subject;
        this.sprint = sprint;
        this.source = source == null ? MindVaultItemSource.PLANNED : source;
        this.learningType = this.source == MindVaultItemSource.RANDOM ? MindVaultLearningType.RANDOM_LEARNING : MindVaultLearningType.IMPORTANT_TOPIC;
        this.title = title;
        this.prompt = prompt;
        this.answer = answer;
        this.notes = notes;
        this.tags = tags;
        this.priority = priority == null ? 3 : priority;
        this.importance = this.priority;
        this.difficulty = difficulty == null ? 3 : difficulty;
        this.masteryScore = masteryScore == null ? 15 : masteryScore;
        this.reviewStreak = reviewStreak == null ? 0 : reviewStreak;
        this.reviewCount = reviewCount == null ? 0 : reviewCount;
        this.successCount = successCount == null ? 0 : successCount;
        this.easeFactor = easeFactor == null ? 2.1d : easeFactor;
        this.reviewIntervalDays = reviewIntervalDays == null ? 1 : reviewIntervalDays;
        this.nextReviewDate = nextReviewDate;
        this.lastReviewedAt = lastReviewedAt;
        this.lastRating = lastRating;
        this.status = status == null ? MindVaultItemStatus.ACTIVE : status;
        this.dueDate = dueDate;
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

    public MindVaultSubject getSubject() {
        return subject;
    }

    public void setSubject(MindVaultSubject subject) {
        this.subject = subject;
    }

    public MindVaultSprint getSprint() {
        return sprint;
    }

    public void setSprint(MindVaultSprint sprint) {
        this.sprint = sprint;
    }

    public MindVaultItemSource getSource() {
        return source;
    }

    public void setSource(MindVaultItemSource source) {
        this.source = source;
    }

    public MindVaultLearningType getLearningType() {
        return learningType;
    }

    public void setLearningType(MindVaultLearningType learningType) {
        this.learningType = learningType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getImportance() {
        return importance;
    }

    public void setImportance(Integer importance) {
        this.importance = importance;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getMasteryScore() {
        return masteryScore;
    }

    public void setMasteryScore(Integer masteryScore) {
        this.masteryScore = masteryScore;
    }

    public Integer getReviewStreak() {
        return reviewStreak;
    }

    public void setReviewStreak(Integer reviewStreak) {
        this.reviewStreak = reviewStreak;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getLapseCount() {
        return lapseCount;
    }

    public void setLapseCount(Integer lapseCount) {
        this.lapseCount = lapseCount;
    }

    public Double getEaseFactor() {
        return easeFactor;
    }

    public void setEaseFactor(Double easeFactor) {
        this.easeFactor = easeFactor;
    }

    public Integer getReviewIntervalDays() {
        return reviewIntervalDays;
    }

    public void setReviewIntervalDays(Integer reviewIntervalDays) {
        this.reviewIntervalDays = reviewIntervalDays;
    }

    public LocalDate getNextReviewDate() {
        return nextReviewDate;
    }

    public void setNextReviewDate(LocalDate nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }

    public Instant getLastReviewedAt() {
        return lastReviewedAt;
    }

    public void setLastReviewedAt(Instant lastReviewedAt) {
        this.lastReviewedAt = lastReviewedAt;
    }

    public Integer getLastRating() {
        return lastRating;
    }

    public void setLastRating(Integer lastRating) {
        this.lastRating = lastRating;
    }

    public boolean isReviewEnabled() {
        return reviewEnabled;
    }

    public void setReviewEnabled(boolean reviewEnabled) {
        this.reviewEnabled = reviewEnabled;
    }

    public String getSourceLabel() {
        return sourceLabel;
    }

    public void setSourceLabel(String sourceLabel) {
        this.sourceLabel = sourceLabel;
    }

    public MindVaultItemStatus getStatus() {
        return status;
    }

    public void setStatus(MindVaultItemStatus status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public List<MindVaultReviewLog> getReviewLogs() {
        return reviewLogs;
    }

    public void setReviewLogs(List<MindVaultReviewLog> reviewLogs) {
        this.reviewLogs = reviewLogs;
    }

    public List<MindVaultResource> getResources() {
        return resources;
    }

    public void setResources(List<MindVaultResource> resources) {
        this.resources = resources;
    }
}
