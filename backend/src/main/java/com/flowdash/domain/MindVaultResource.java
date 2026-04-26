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
@Table(name = "mindvault_resource")
public class MindVaultResource extends AuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id")
    private MindVaultLearningItem item;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private MindVaultResourceType resourceType = MindVaultResourceType.TEXT;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String url;

    @Column(name = "storage_path")
    private String storagePath;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "original_file_name")
    private String originalFileName;

    protected MindVaultResource() {
    }

    public MindVaultResource(AppUser user, MindVaultLearningItem item, MindVaultResourceType resourceType, String title, String description, String url, String storagePath, String mimeType, Long sizeBytes, String originalFileName) {
        this.user = user;
        this.item = item;
        this.resourceType = resourceType == null ? MindVaultResourceType.TEXT : resourceType;
        this.title = title;
        this.description = description;
        this.url = url;
        this.storagePath = storagePath;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.originalFileName = originalFileName;
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

    public MindVaultLearningItem getItem() {
        return item;
    }

    public MindVaultResourceType getResourceType() {
        return resourceType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }
}
