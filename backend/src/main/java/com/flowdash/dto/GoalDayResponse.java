package com.flowdash.dto;

import java.time.LocalDate;
import java.util.List;

public record GoalDayResponse(
        LocalDate date,
        long activeGoalCount,
        long progressedGoalCount,
        List<Long> progressedGoalIds
) {
}
