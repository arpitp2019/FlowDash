package com.flowdash.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "habit_item")
public class HabitItem extends AuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HabitCadence cadence = HabitCadence.DAILY;

    @Column(name = "target_count", nullable = false)
    private Integer targetCount = 1;

    @Column(nullable = false)
    private Integer streak = 0;

    @Column(name = "completed_count", nullable = false)
    private Integer completedCount = 0;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(nullable = false)
    private boolean archived = false;

    protected HabitItem() {
    }

    public HabitItem(AppUser user, String title, HabitCadence cadence, Integer targetCount, Integer streak, Integer completedCount, String notes, boolean archived) {
        this.user = user;
        this.title = title;
        this.cadence = cadence == null ? HabitCadence.DAILY : cadence;
        this.targetCount = targetCount == null ? 1 : targetCount;
        this.streak = streak == null ? 0 : streak;
        this.completedCount = completedCount == null ? 0 : completedCount;
        this.notes = notes;
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

    public HabitCadence getCadence() {
        return cadence;
    }

    public void setCadence(HabitCadence cadence) {
        this.cadence = cadence;
    }

    public Integer getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(Integer targetCount) {
        this.targetCount = targetCount;
    }

    public Integer getStreak() {
        return streak;
    }

    public void setStreak(Integer streak) {
        this.streak = streak;
    }

    public Integer getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
