package com.flowdash.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

@Entity
@Table(
        name = "goal_activity",
        uniqueConstraints = @UniqueConstraint(name = "uk_goal_activity_goal_date", columnNames = {"goal_id", "activity_date"})
)
public class GoalActivity extends AuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "goal_id")
    private GoalItem goal;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(columnDefinition = "text")
    private String note;

    protected GoalActivity() {
    }

    public GoalActivity(AppUser user, GoalItem goal, LocalDate activityDate, String note) {
        this.user = user;
        this.goal = goal;
        this.activityDate = activityDate;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public GoalItem getGoal() {
        return goal;
    }

    public LocalDate getActivityDate() {
        return activityDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
