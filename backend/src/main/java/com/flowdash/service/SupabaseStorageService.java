package com.flowdash.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    private static final long MAX_FILE_SIZE_BYTES = 25L * 1024L * 1024L;

    private final HttpClient httpClient;
    private final String supabaseUrl;
    private final String serviceRoleKey;
    private final String bucket;

    public SupabaseStorageService(
            @Value("${flowdash.supabase.url:${SUPABASE_URL:}}") String supabaseUrl,
            @Value("${flowdash.supabase.service-role-key:${SUPABASE_SERVICE_ROLE_KEY:}}") String serviceRoleKey,
            @Value("${flowdash.supabase.storage-bucket:${SUPABASE_STORAGE_BUCKET:}}") String bucket
    ) {
        this.httpClient = HttpClient.newHttpClient();
        this.supabaseUrl = trimTrailingSlash(supabaseUrl);
        this.serviceRoleKey = serviceRoleKey == null ? "" : serviceRoleKey.trim();
        this.bucket = bucket == null ? "" : bucket.trim();
    }

    public boolean isConfigured() {
        return !supabaseUrl.isBlank() && !serviceRoleKey.isBlank() && !bucket.isBlank();
    }

    public StoredObject upload(Long userId, Long itemId, MultipartFile file) {
        if (!isConfigured()) {
            throw new IllegalStateException("Supabase Storage is not configured");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose a file to upload");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File must be 25 MB or smaller");
        }

        String originalName = file.getOriginalFilename() == null ? "resource" : file.getOriginalFilename();
        String path = "mindvault/%d/%d/%s-%s".formatted(userId, itemId, UUID.randomUUID(), sanitizeFileName(originalName));
        String contentType = file.getContentType() == null || file.getContentType().isBlank()
                ? "application/octet-stream"
                : file.getContentType();

        try {
            HttpRequest request = HttpRequest.newBuilder(uploadUri(path))
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .header("Content-Type", contentType)
                    .header("x-upsert", "false")
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Supabase upload failed: " + response.body());
            }
            return new StoredObject(path, contentType, file.getSize(), originalName);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read uploaded file", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Supabase upload was interrupted", exception);
        }
    }

    public void delete(String storagePath) {
        if (!isConfigured() || storagePath == null || storagePath.isBlank()) {
            return;
        }
        String body = "{\"prefixes\":[\"" + escapeJson(storagePath) + "\"]}";
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create("%s/storage/v1/object/%s".formatted(supabaseUrl, encodePath(bucket))))
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .header("Content-Type", "application/json")
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(body))
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
            // Metadata deletion should still succeed if the remote object is already gone.
        }
    }

    private URI uploadUri(String path) {
        return URI.create("%s/storage/v1/object/%s/%s".formatted(supabaseUrl, encodePath(bucket), encodePath(path)));
    }

    private static String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("/+$", "");
    }

    private static String sanitizeFileName(String value) {
        String sanitized = value.replace('\\', '/');
        int slash = sanitized.lastIndexOf('/');
        if (slash >= 0) {
            sanitized = sanitized.substring(slash + 1);
        }
        sanitized = sanitized.replaceAll("[^A-Za-z0-9._-]", "-");
        return sanitized.isBlank() ? "resource" : sanitized;
    }

    private static String encodePath(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20").replace("%2F", "/");
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public record StoredObject(String storagePath, String mimeType, Long sizeBytes, String originalFileName) {
    }
}
