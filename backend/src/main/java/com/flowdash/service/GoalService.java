package com.flowdash.service;

import com.flowdash.domain.AppUser;
import com.flowdash.domain.GoalItem;
import com.flowdash.dto.GoalRequest;
import com.flowdash.repository.GoalRepository;
import com.flowdash.security.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final CurrentUserService currentUserService;

    public GoalService(GoalRepository goalRepository, CurrentUserService currentUserService) {
        this.goalRepository = goalRepository;
        this.currentUserService = currentUserService;
    }

    public List<GoalItem> list() {
        return goalRepository.findAllByUserIdOrderByUpdatedAtDesc(currentUserService.requireCurrentUserId());
    }

    public GoalItem create(GoalRequest request) {
        AppUser user = currentUserService.requireCurrentUser();
        GoalItem item = new GoalItem(user, request.title(), request.description(), request.status(), request.priority(), request.dueDate());
        return goalRepository.save(item);
    }

    public GoalItem update(Long id, GoalRequest request) {
        GoalItem item = requireOwnedGoal(id);
        item.setTitle(request.title());
        item.setDescription(request.description());
        item.setStatus(request.status() == null ? item.getStatus() : request.status());
        item.setPriority(request.priority() == null ? item.getPriority() : request.priority());
        item.setDueDate(request.dueDate());
        return goalRepository.save(item);
    }

    public void delete(Long id) {
        GoalItem item = requireOwnedGoal(id);
        goalRepository.delete(item);
    }

    private GoalItem requireOwnedGoal(Long id) {
        GoalItem item = goalRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Goal not found"));
        if (!item.getUser().getId().equals(currentUserService.requireCurrentUserId())) {
            throw new AccessDeniedException("Goal does not belong to the current user");
        }
        return item;
    }
}
