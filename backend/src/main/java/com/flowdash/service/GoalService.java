package com.flowdash.service;

import com.flowdash.domain.AppUser;
import com.flowdash.domain.GoalActivity;
import com.flowdash.domain.GoalItem;
import com.flowdash.domain.GoalStatus;
import com.flowdash.dto.GoalActivityRequest;
import com.flowdash.dto.GoalActivityResponse;
import com.flowdash.dto.GoalAnalyticsResponse;
import com.flowdash.dto.GoalDayResponse;
import com.flowdash.dto.GoalOverviewItemResponse;
import com.flowdash.dto.GoalOverviewResponse;
import com.flowdash.dto.GoalRequest;
import com.flowdash.dto.GoalStatsResponse;
import com.flowdash.repository.GoalActivityRepository;
import com.flowdash.repository.GoalRepository;
import com.flowdash.security.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalActivityRepository activityRepository;
    private final CurrentUserService currentUserService;

    public GoalService(GoalRepository goalRepository, GoalActivityRepository activityRepository, CurrentUserService currentUserService) {
        this.goalRepository = goalRepository;
        this.activityRepository = activityRepository;
        this.currentUserService = currentUserService;
    }

    public List<GoalItem> list() {
        return goalRepository.findAllByUserIdOrderByUpdatedAtDesc(currentUserService.requireCurrentUserId());
    }

    public GoalOverviewResponse overview(LocalDate date, Integer year) {
        LocalDate selectedDate = normalizeDate(date);
        int selectedYear = year == null ? selectedDate.getYear() : year;
        LocalDate yearStart = LocalDate.of(selectedYear, 1, 1);
        LocalDate yearEnd = LocalDate.of(selectedYear, 12, 31);
        LocalDate metricStart = minDate(yearStart, selectedDate.minusDays(365));
        Long userId = currentUserService.requireCurrentUserId();
        List<GoalItem> goals = goalRepository.findAllByUserIdOrderByUpdatedAtDesc(userId);
        List<GoalActivity> activities = activityRepository.findAllByUserIdAndActivityDateBetweenOrderByActivityDateDesc(userId, metricStart, yearEnd);
        Map<Long, Set<LocalDate>> activityDatesByGoal = groupActivityDates(activities);
        List<GoalOverviewItemResponse> mapped = goals.stream()
                .map(goal -> response(goal, activityDatesByGoal.getOrDefault(goal.getId(), Set.of()), selectedDate, yearStart, yearEnd))
                .toList();
        List<GoalOverviewItemResponse> checklist = mapped.stream()
                .filter(GoalOverviewItemResponse::active)
                .sorted(GoalService::sortChecklist)
                .toList();
        List<GoalDayResponse> calendarDays = calendarDays(goals, activityDatesByGoal, yearStart, yearEnd);
        return new GoalOverviewResponse(stats(mapped, calendarDays, selectedDate, selectedYear), analytics(mapped), mapped, checklist, calendarDays);
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
        activityRepository.deleteAllByGoalIdAndUserId(id, currentUserService.requireCurrentUserId());
        goalRepository.delete(item);
    }

    public GoalActivityResponse markActivity(Long id, GoalActivityRequest request) {
        GoalItem goal = requireOwnedGoal(id);
        AppUser user = currentUserService.requireCurrentUser();
        LocalDate activityDate = request.activityDate() == null ? normalizeDate(null) : request.activityDate();
        GoalActivity activity = activityRepository.findByGoalIdAndUserIdAndActivityDate(id, user.getId(), activityDate)
                .orElseGet(() -> new GoalActivity(user, goal, activityDate, null));
        activity.setNote(normalizeText(request.note()));
        if (goal.getStatus() == GoalStatus.PLANNED) {
            goal.setStatus(GoalStatus.IN_PROGRESS);
            goalRepository.save(goal);
        }
        return toActivityResponse(activityRepository.save(activity));
    }

    public void clearActivity(Long id, LocalDate date) {
        requireOwnedGoal(id);
        Long userId = currentUserService.requireCurrentUserId();
        LocalDate activityDate = date == null ? normalizeDate(null) : date;
        activityRepository.findByGoalIdAndUserIdAndActivityDate(id, userId, activityDate)
                .ifPresent(activityRepository::delete);
    }

    public List<GoalActivityResponse> activities(Long id, LocalDate from, LocalDate to) {
        requireOwnedGoal(id);
        LocalDate end = to == null ? normalizeDate(null) : to;
        LocalDate start = from == null ? end.minusDays(30) : from;
        return activityRepository.findAllByGoalIdAndUserIdAndActivityDateBetweenOrderByActivityDateDesc(id, currentUserService.requireCurrentUserId(), start, end)
                .stream()
                .map(GoalService::toActivityResponse)
                .toList();
    }

    static boolean isActive(GoalItem goal) {
        return goal.getStatus() != GoalStatus.DONE;
    }

    private static GoalOverviewItemResponse response(GoalItem goal, Set<LocalDate> activityDates, LocalDate selectedDate, LocalDate yearStart, LocalDate yearEnd) {
        boolean active = isActive(goal);
        boolean progressedToday = activityDates.contains(selectedDate);
        boolean overdue = active && goal.getDueDate() != null && goal.getDueDate().isBefore(selectedDate);
        return new GoalOverviewItemResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getStatus(),
                goal.getPriority(),
                goal.getDueDate(),
                active,
                progressedToday,
                overdue,
                streakEndingOn(activityDates, selectedDate),
                bestStreak(activityDates, yearStart, yearEnd),
                consistency(activityDates, selectedDate.minusDays(6), selectedDate),
                consistency(activityDates, selectedDate.minusDays(29), selectedDate),
                (int) activityDates.stream().filter(date -> !date.isBefore(yearStart) && !date.isAfter(yearEnd)).count(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }

    private static GoalStatsResponse stats(List<GoalOverviewItemResponse> goals, List<GoalDayResponse> calendarDays, LocalDate selectedDate, int year) {
        long active = goals.stream().filter(GoalOverviewItemResponse::active).count();
        long complete = goals.stream().filter(goal -> goal.status() == GoalStatus.DONE).count();
        long progressed = goals.stream().filter(GoalOverviewItemResponse::progressedToday).count();
        long overdue = goals.stream().filter(GoalOverviewItemResponse::overdue).count();
        int todayProgress = active == 0 ? 100 : (int) Math.round(progressed * 100.0 / active);
        Set<LocalDate> activeDates = calendarDays.stream()
                .filter(day -> day.progressedGoalCount() > 0)
                .map(GoalDayResponse::date)
                .collect(Collectors.toSet());
        int weekly = aggregateConsistency(calendarDays, selectedDate.minusDays(6), selectedDate);
        int monthly = aggregateConsistency(calendarDays, selectedDate.minusDays(29), selectedDate);
        int currentStreak = streakEndingOn(activeDates, selectedDate);
        int bestStreak = bestStreak(activeDates, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
        return new GoalStatsResponse(goals.size(), active, complete, progressed, overdue, todayProgress, weekly, monthly, activeDates.size(), currentStreak, bestStreak);
    }

    private static GoalAnalyticsResponse analytics(List<GoalOverviewItemResponse> goals) {
        Map<String, Long> statusMix = goals.stream().collect(Collectors.groupingBy(goal -> goal.status().name(), Collectors.counting()));
        Map<String, Long> priorityMix = goals.stream().collect(Collectors.groupingBy(goal -> "P" + goal.priority(), Collectors.counting()));
        List<GoalOverviewItemResponse> overdue = goals.stream()
                .filter(GoalOverviewItemResponse::overdue)
                .sorted(Comparator.comparing(GoalOverviewItemResponse::dueDate, Comparator.nullsLast(LocalDate::compareTo)))
                .limit(6)
                .toList();
        List<GoalOverviewItemResponse> topStreaks = goals.stream()
                .filter(goal -> goal.bestStreak() > 0)
                .sorted(Comparator.comparing(GoalOverviewItemResponse::bestStreak).reversed())
                .limit(6)
                .toList();
        return new GoalAnalyticsResponse(statusMix, priorityMix, overdue, topStreaks);
    }

    private static List<GoalDayResponse> calendarDays(List<GoalItem> goals, Map<Long, Set<LocalDate>> activityDatesByGoal, LocalDate from, LocalDate to) {
        int days = (int) (to.toEpochDay() - from.toEpochDay());
        return IntStream.rangeClosed(0, days)
                .mapToObj(offset -> {
                    LocalDate date = from.plusDays(offset);
                    List<GoalItem> activeGoals = goals.stream()
                            .filter(GoalService::isActive)
                            .filter(goal -> goal.getCreatedAt() == null || !goal.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate().isAfter(date))
                            .toList();
                    List<Long> progressedGoalIds = activeGoals.stream()
                            .filter(goal -> activityDatesByGoal.getOrDefault(goal.getId(), Set.of()).contains(date))
                            .map(GoalItem::getId)
                            .toList();
                    return new GoalDayResponse(date, activeGoals.size(), progressedGoalIds.size(), progressedGoalIds);
                })
                .toList();
    }

    private static int aggregateConsistency(List<GoalDayResponse> days, LocalDate from, LocalDate to) {
        int active = 0;
        int progressed = 0;
        for (GoalDayResponse day : days) {
            if (day.date().isBefore(from) || day.date().isAfter(to) || day.activeGoalCount() == 0) {
                continue;
            }
            active += day.activeGoalCount();
            progressed += day.progressedGoalCount();
        }
        return active == 0 ? 100 : (int) Math.round(progressed * 100.0 / active);
    }

    private static int consistency(Set<LocalDate> dates, LocalDate from, LocalDate to) {
        int total = 0;
        int hits = 0;
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            total++;
            if (dates.contains(cursor)) {
                hits++;
            }
            cursor = cursor.plusDays(1);
        }
        return total == 0 ? 0 : (int) Math.round(hits * 100.0 / total);
    }

    private static int streakEndingOn(Set<LocalDate> dates, LocalDate date) {
        int streak = 0;
        LocalDate cursor = date;
        while (dates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private static int bestStreak(Set<LocalDate> dates, LocalDate from, LocalDate to) {
        int best = 0;
        int current = 0;
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            if (dates.contains(cursor)) {
                current++;
                best = Math.max(best, current);
            } else {
                current = 0;
            }
            cursor = cursor.plusDays(1);
        }
        return best;
    }

    private static Map<Long, Set<LocalDate>> groupActivityDates(List<GoalActivity> activities) {
        Map<Long, Set<LocalDate>> grouped = new HashMap<>();
        for (GoalActivity activity : activities) {
            grouped.computeIfAbsent(activity.getGoal().getId(), ignored -> new HashSet<>()).add(activity.getActivityDate());
        }
        return grouped;
    }

    private static GoalActivityResponse toActivityResponse(GoalActivity activity) {
        return new GoalActivityResponse(activity.getId(), activity.getGoal().getId(), activity.getActivityDate(), activity.getNote(), activity.getCreatedAt(), activity.getUpdatedAt());
    }

    private GoalItem requireOwnedGoal(Long id) {
        GoalItem item = goalRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Goal not found"));
        if (!item.getUser().getId().equals(currentUserService.requireCurrentUserId())) {
            throw new AccessDeniedException("Goal does not belong to the current user");
        }
        return item;
    }

    private static int sortChecklist(GoalOverviewItemResponse left, GoalOverviewItemResponse right) {
        int progress = Boolean.compare(left.progressedToday(), right.progressedToday());
        if (progress != 0) {
            return progress;
        }
        int due = Comparator.comparing(GoalOverviewItemResponse::dueDate, Comparator.nullsLast(LocalDate::compareTo)).compare(left, right);
        return due != 0 ? due : Integer.compare(right.priority(), left.priority());
    }

    private static LocalDate normalizeDate(LocalDate date) {
        return date == null ? LocalDate.now(ZoneOffset.UTC) : date;
    }

    private static LocalDate minDate(LocalDate left, LocalDate right) {
        return left.isBefore(right) ? left : right;
    }

    private static String normalizeText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
