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

import java.math.BigDecimal;

@Entity
@Table(name = "ai_provider_setting")
public class AiProviderSetting extends AuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_key", nullable = false)
    private AiProviderKey providerKey = AiProviderKey.MOCK;

    @Column(name = "provider_label", nullable = false)
    private String providerLabel = "Mock";

    @Column(name = "model_name", nullable = false)
    private String modelName = "flowdash-mock";

    @Column(name = "base_url")
    private String baseUrl;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "default_selected", nullable = false)
    private boolean defaultSelected = true;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal temperature = new BigDecimal("0.30");

    protected AiProviderSetting() {
    }

    public AiProviderSetting(AppUser user, AiProviderKey providerKey, String providerLabel, String modelName, String baseUrl, boolean enabled, boolean defaultSelected, BigDecimal temperature) {
        this.user = user;
        this.providerKey = providerKey;
        this.providerLabel = providerLabel;
        this.modelName = modelName;
        this.baseUrl = baseUrl;
        this.enabled = enabled;
        this.defaultSelected = defaultSelected;
        this.temperature = temperature;
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

    public AiProviderKey getProviderKey() {
        return providerKey;
    }

    public void setProviderKey(AiProviderKey providerKey) {
        this.providerKey = providerKey;
    }

    public String getProviderLabel() {
        return providerLabel;
    }

    public void setProviderLabel(String providerLabel) {
        this.providerLabel = providerLabel;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDefaultSelected() {
        return defaultSelected;
    }

    public void setDefaultSelected(boolean defaultSelected) {
        this.defaultSelected = defaultSelected;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }
}
