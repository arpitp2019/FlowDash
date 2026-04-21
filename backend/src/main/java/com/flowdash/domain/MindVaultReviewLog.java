package com.flowdash.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "mindvault_review_log")
public class MindVaultReviewLog extends AuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id")
    private MindVaultLearningItem item;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "previous_interval_days")
    private Integer previousIntervalDays;

    @Column(name = "next_interval_days")
    private Integer nextIntervalDays;

    @Column(name = "mastery_after")
    private Integer masteryAfter;

    @Column(name = "ease_factor_after")
    private Double easeFactorAfter;

    @Column(columnDefinition = "text")
    private String note;

    protected MindVaultReviewLog() {
    }

    public MindVaultReviewLog(AppUser user, MindVaultLearningItem item, Integer rating, Integer previousIntervalDays, Integer nextIntervalDays, Integer masteryAfter, Double easeFactorAfter, String note) {
        this.user = user;
        this.item = item;
        this.rating = rating;
        this.previousIntervalDays = previousIntervalDays;
        this.nextIntervalDays = nextIntervalDays;
        this.masteryAfter = masteryAfter;
        this.easeFactorAfter = easeFactorAfter;
        this.note = note;
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

    public MindVaultLearningItem getItem() {
        return item;
    }

    public void setItem(MindVaultLearningItem item) {
        this.item = item;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getPreviousIntervalDays() {
        return previousIntervalDays;
    }

    public void setPreviousIntervalDays(Integer previousIntervalDays) {
        this.previousIntervalDays = previousIntervalDays;
    }

    public Integer getNextIntervalDays() {
        return nextIntervalDays;
    }

    public void setNextIntervalDays(Integer nextIntervalDays) {
        this.nextIntervalDays = nextIntervalDays;
    }

    public Integer getMasteryAfter() {
        return masteryAfter;
    }

    public void setMasteryAfter(Integer masteryAfter) {
        this.masteryAfter = masteryAfter;
    }

    public Double getEaseFactorAfter() {
        return easeFactorAfter;
    }

    public void setEaseFactorAfter(Double easeFactorAfter) {
        this.easeFactorAfter = easeFactorAfter;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
