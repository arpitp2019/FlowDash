package com.flowdash.service;

import com.flowdash.domain.AppUser;
import com.flowdash.domain.AuthProvider;
import com.flowdash.domain.GoalActivity;
import com.flowdash.domain.GoalItem;
import com.flowdash.domain.GoalStatus;
import com.flowdash.dto.GoalActivityRequest;
import com.flowdash.repository.GoalActivityRepository;
import com.flowdash.repository.GoalRepository;
import com.flowdash.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalActivityRepository activityRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private GoalService service;

    @Test
    void overviewBuildsChecklistYearCalendarAndAnalytics() {
        LocalDate today = LocalDate.of(2026, 4, 21);
        AppUser user = user(1L);
        GoalItem first = goal(user, 10L, "Ship FlowDash", GoalStatus.IN_PROGRESS, today.plusDays(7));
        GoalItem second = goal(user, 11L, "Portfolio", GoalStatus.PLANNED, today.minusDays(1));
        GoalActivity yesterday = new GoalActivity(user, first, today.minusDays(1), null);
        GoalActivity todayActivity = new GoalActivity(user, first, today, null);

        when(currentUserService.requireCurrentUserId()).thenReturn(1L);
        when(goalRepository.findAllByUserIdOrderByUpdatedAtDesc(1L)).thenReturn(List.of(first, second));
        when(activityRepository.findAllByUserIdAndActivityDateBetweenOrderByActivityDateDesc(any(), any(), any())).thenReturn(List.of(yesterday, todayActivity));

        var overview = service.overview(today, 2026);

        assertThat(overview.checklist()).hasSize(2);
        assertThat(overview.stats().progressedToday()).isEqualTo(1);
        assertThat(overview.stats().todayProgress()).isEqualTo(50);
        assertThat(overview.stats().overdueGoals()).isEqualTo(1);
        assertThat(overview.stats().currentStreak()).isGreaterThanOrEqualTo(2);
        assertThat(overview.calendarDays()).hasSize(365);
        assertThat(overview.calendarDays().stream().filter(day -> today.equals(day.date())).findFirst().orElseThrow().progressedGoalCount()).isEqualTo(1);
        assertThat(overview.analytics().overdueGoals()).extracting("title").contains("Portfolio");
    }

    @Test
    void markActivityUpsertsOneDateAndMovesPlannedGoalToInProgress() {
        LocalDate today = LocalDate.of(2026, 4, 21);
        AppUser user = user(1L);
        GoalItem goal = goal(user, 12L, "Read docs", GoalStatus.PLANNED, null);

        when(currentUserService.requireCurrentUserId()).thenReturn(1L);
        when(currentUserService.requireCurrentUser()).thenReturn(user);
        when(goalRepository.findById(12L)).thenReturn(Optional.of(goal));
        when(activityRepository.findByGoalIdAndUserIdAndActivityDate(12L, 1L, today)).thenReturn(Optional.empty());
        when(activityRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.markActivity(12L, new GoalActivityRequest(today, "made progress"));

        assertThat(response.goalId()).isEqualTo(12L);
        assertThat(goal.getStatus()).isEqualTo(GoalStatus.IN_PROGRESS);
        verify(goalRepository).save(goal);
        verify(activityRepository).save(any(GoalActivity.class));
    }

    @Test
    void crossUserGoalActivityIsRejected() {
        GoalItem goal = goal(user(2L), 13L, "Private", GoalStatus.PLANNED, null);

        when(currentUserService.requireCurrentUserId()).thenReturn(1L);
        when(goalRepository.findById(13L)).thenReturn(Optional.of(goal));

        assertThatThrownBy(() -> service.clearActivity(13L, LocalDate.now()))
                .isInstanceOf(AccessDeniedException.class);
    }

    private static GoalItem goal(AppUser user, Long id, String title, GoalStatus status, LocalDate dueDate) {
        GoalItem goal = new GoalItem(user, title, null, status, 3, dueDate);
        goal.setId(id);
        goal.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        goal.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        return goal;
    }

    private static AppUser user(Long id) {
        AppUser user = new AppUser("user" + id + "@example.com", "User " + id, null, AuthProvider.LOCAL, null);
        user.setId(id);
        return user;
    }
}
