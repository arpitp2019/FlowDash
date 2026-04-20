package com.flowdash.service;

import com.flowdash.domain.AppUser;
import com.flowdash.domain.HabitItem;
import com.flowdash.dto.HabitRequest;
import com.flowdash.repository.HabitRepository;
import com.flowdash.security.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HabitService {

    private final HabitRepository habitRepository;
    private final CurrentUserService currentUserService;

    public HabitService(HabitRepository habitRepository, CurrentUserService currentUserService) {
        this.habitRepository = habitRepository;
        this.currentUserService = currentUserService;
    }

    public List<HabitItem> list() {
        return habitRepository.findAllByUserIdOrderByUpdatedAtDesc(currentUserService.requireCurrentUserId());
    }

    public HabitItem create(HabitRequest request) {
        AppUser user = currentUserService.requireCurrentUser();
        HabitItem item = new HabitItem(user, request.title(), request.cadence(), request.targetCount(), request.streak(), request.completedCount(), request.notes(), request.archived() != null && request.archived());
        return habitRepository.save(item);
    }

    public HabitItem update(Long id, HabitRequest request) {
        HabitItem item = requireOwnedHabit(id);
        item.setTitle(request.title());
        if (request.cadence() != null) {
            item.setCadence(request.cadence());
        }
        item.setTargetCount(request.targetCount() == null ? item.getTargetCount() : request.targetCount());
        item.setStreak(request.streak() == null ? item.getStreak() : request.streak());
        item.setCompletedCount(request.completedCount() == null ? item.getCompletedCount() : request.completedCount());
        item.setNotes(request.notes());
        if (request.archived() != null) {
            item.setArchived(request.archived());
        }
        return habitRepository.save(item);
    }

    public void delete(Long id) {
        HabitItem item = requireOwnedHabit(id);
        habitRepository.delete(item);
    }

    private HabitItem requireOwnedHabit(Long id) {
        HabitItem item = habitRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Habit not found"));
        if (!item.getUser().getId().equals(currentUserService.requireCurrentUserId())) {
            throw new AccessDeniedException("Habit does not belong to the current user");
        }
        return item;
    }
}
