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
@Table(name = "decision_message")
public class DecisionMessage extends AuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "thread_id")
    private DecisionThread thread;

    @Column(nullable = false)
    private String role;

    @Column(name = "tab_key", nullable = false)
    private String tabKey;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column
    private String model;

    protected DecisionMessage() {
    }

    public DecisionMessage(DecisionThread thread, String role, String tabKey, String content, String model) {
        this.thread = thread;
        this.role = role;
        this.tabKey = tabKey;
        this.content = content;
        this.model = model;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DecisionThread getThread() {
        return thread;
    }

    public void setThread(DecisionThread thread) {
        this.thread = thread;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTabKey() {
        return tabKey;
    }

    public void setTabKey(String tabKey) {
        this.tabKey = tabKey;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
