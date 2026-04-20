package com.flowdash.service;

import com.flowdash.domain.AppUser;
import com.flowdash.domain.DecisionMessage;
import com.flowdash.domain.DecisionStatus;
import com.flowdash.domain.DecisionThread;
import com.flowdash.dto.DecisionMessageRequest;
import com.flowdash.dto.DecisionThreadRequest;
import com.flowdash.repository.DecisionMessageRepository;
import com.flowdash.repository.DecisionThreadRepository;
import com.flowdash.security.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class DecisionService {

    private static final int SUMMARY_LIMIT = 220;

    private final DecisionThreadRepository threadRepository;
    private final DecisionMessageRepository messageRepository;
    private final CurrentUserService currentUserService;

    public DecisionService(DecisionThreadRepository threadRepository,
                           DecisionMessageRepository messageRepository,
                           CurrentUserService currentUserService) {
        this.threadRepository = threadRepository;
        this.messageRepository = messageRepository;
        this.currentUserService = currentUserService;
    }

    public List<DecisionThread> listThreads() {
        return threadRepository.findAllByUserIdOrderByLastActiveAtDesc(currentUserService.requireCurrentUserId());
    }

    public DecisionThread createThread(DecisionThreadRequest request, String providerKey, String providerModel) {
        AppUser user = currentUserService.requireCurrentUser();
        DecisionThread thread = new DecisionThread(user, request.title());
        thread.setProviderKey(providerKey);
        thread.setProviderModel(providerModel);
        thread.setStatus(DecisionStatus.ACTIVE);
        thread.setLastActiveAt(Instant.now());
        return threadRepository.save(thread);
    }

    public DecisionThread requireOwnedThread(Long id) {
        DecisionThread thread = threadRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Decision thread not found"));
        if (!thread.getUser().getId().equals(currentUserService.requireCurrentUserId())) {
            throw new AccessDeniedException("Thread does not belong to the current user");
        }
        return thread;
    }

    public DecisionThread requireOwnedThread(Long id, Long userId) {
        DecisionThread thread = threadRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Decision thread not found"));
        if (!thread.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Thread does not belong to the current user");
        }
        return thread;
    }

    public DecisionThread updateThread(Long id, DecisionThreadRequest request) {
        DecisionThread thread = requireOwnedThread(id);
        thread.setTitle(request.title());
        thread.setLastActiveAt(Instant.now());
        return threadRepository.save(thread);
    }

    public void deleteThread(Long id) {
        DecisionThread thread = requireOwnedThread(id);
        threadRepository.delete(thread);
    }

    public List<DecisionMessage> listMessages(Long threadId) {
        requireOwnedThread(threadId);
        return messageRepository.findAllByThreadIdOrderByCreatedAtAsc(threadId);
    }

    public List<DecisionMessage> listMessages(DecisionThread thread) {
        return messageRepository.findAllByThreadIdOrderByCreatedAtAsc(thread.getId());
    }

    public DecisionMessage addMessage(Long threadId, DecisionMessageRequest request, String role, String model) {
        DecisionThread thread = requireOwnedThread(threadId);
        DecisionMessage message = new DecisionMessage(thread, role, request.tabKey(), request.content(), model);
        thread.setLastActiveAt(Instant.now());
        threadRepository.save(thread);
        return messageRepository.save(message);
    }

    public DecisionMessage addMessage(DecisionThread thread, DecisionMessageRequest request, String role, String model) {
        DecisionMessage message = new DecisionMessage(thread, role, request.tabKey(), request.content(), model);
        thread.setLastActiveAt(Instant.now());
        threadRepository.save(thread);
        return messageRepository.save(message);
    }

    public DecisionMessage addGeneratedMessage(Long threadId, String tabKey, String content, String model) {
        DecisionThread thread = requireOwnedThread(threadId);
        DecisionMessage message = new DecisionMessage(thread, "assistant", tabKey, content, model);
        thread.setSummary(summarize(content));
        thread.setMemoGeneratedAt(Instant.now());
        thread.setLastActiveAt(Instant.now());
        threadRepository.save(thread);
        return messageRepository.save(message);
    }

    public DecisionMessage addGeneratedMessage(DecisionThread thread, String tabKey, String content, String model) {
        DecisionMessage message = new DecisionMessage(thread, "assistant", tabKey, content, model);
        thread.setSummary(summarize(content));
        thread.setMemoGeneratedAt(Instant.now());
        thread.setLastActiveAt(Instant.now());
        threadRepository.save(thread);
        return messageRepository.save(message);
    }

    private static String summarize(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= SUMMARY_LIMIT) {
            return normalized;
        }
        return normalized.substring(0, SUMMARY_LIMIT - 3) + "...";
    }
}
