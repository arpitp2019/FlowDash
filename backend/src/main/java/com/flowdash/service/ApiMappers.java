package com.flowdash.service;

import com.flowdash.domain.AppUser;
import com.flowdash.domain.DecisionMessage;
import com.flowdash.domain.DecisionThread;
import com.flowdash.domain.GoalItem;
import com.flowdash.domain.HabitItem;
import com.flowdash.domain.VaultEntry;
import com.flowdash.dto.DecisionMessageResponse;
import com.flowdash.dto.DecisionThreadResponse;
import com.flowdash.dto.GoalResponse;
import com.flowdash.dto.HabitResponse;
import com.flowdash.dto.UserResponse;
import com.flowdash.dto.VaultResponse;

public final class ApiMappers {

    private ApiMappers() {
    }

    public static UserResponse toUserResponse(AppUser user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getOauthProvider().name());
    }

    public static GoalResponse toGoalResponse(GoalItem item) {
        return new GoalResponse(item.getId(), item.getTitle(), item.getDescription(), item.getStatus(), item.getPriority(), item.getDueDate(), item.getCreatedAt(), item.getUpdatedAt());
    }

    public static HabitResponse toHabitResponse(HabitItem item) {
        return new HabitResponse(item.getId(), item.getTitle(), item.getCadence(), item.getTargetCount(), item.getStreak(), item.getCompletedCount(), item.getNotes(), item.isArchived(), item.getCreatedAt(), item.getUpdatedAt());
    }

    public static VaultResponse toVaultResponse(VaultEntry item) {
        return new VaultResponse(item.getId(), item.getTitle(), item.getContent(), item.getEntryType(), item.getTags(), item.isFavorite(), item.getCreatedAt(), item.getUpdatedAt());
    }

    public static DecisionThreadResponse toDecisionThreadResponse(DecisionThread thread) {
        return new DecisionThreadResponse(thread.getId(), thread.getTitle(), thread.getSummary(), thread.getProviderKey(), thread.getProviderModel(), thread.getStatus(), thread.getMemoGeneratedAt(), thread.getLastActiveAt(), thread.getCreatedAt(), thread.getUpdatedAt());
    }

    public static DecisionMessageResponse toDecisionMessageResponse(DecisionMessage message) {
        return new DecisionMessageResponse(message.getId(), message.getThread().getId(), message.getRole(), message.getTabKey(), message.getContent(), message.getModel(), message.getCreatedAt());
    }
}
