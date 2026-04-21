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

import java.time.LocalDate;

@Entity
@Table(name = "mindvault_sprint")
public class MindVaultSprint extends AuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private MindVaultSubject subject;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MindVaultSprintStatus status = MindVaultSprintStatus.PLANNED;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "estimated_sessions", nullable = false)
    private Integer estimatedSessions = 1;

    @Column(name = "completed_sessions", nullable = false)
    private Integer completedSessions = 0;

    protected MindVaultSprint() {
    }

    public MindVaultSprint(AppUser user, MindVaultSubject subject, String title, String description, MindVaultSprintStatus status, LocalDate startDate, LocalDate dueDate, Integer estimatedSessions, Integer completedSessions) {
        this.user = user;
        this.subject = subject;
        this.title = title;
        this.description = description;
        this.status = status == null ? MindVaultSprintStatus.PLANNED : status;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.estimatedSessions = estimatedSessions == null ? 1 : estimatedSessions;
        this.completedSessions = completedSessions == null ? 0 : completedSessions;
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

    public MindVaultSprintStatus getStatus() {
        return status;
    }

    public void setStatus(MindVaultSprintStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getEstimatedSessions() {
        return estimatedSessions;
    }

    public void setEstimatedSessions(Integer estimatedSessions) {
        this.estimatedSessions = estimatedSessions;
    }

    public Integer getCompletedSessions() {
        return completedSessions;
    }

    public void setCompletedSessions(Integer completedSessions) {
        this.completedSessions = completedSessions;
    }
}
