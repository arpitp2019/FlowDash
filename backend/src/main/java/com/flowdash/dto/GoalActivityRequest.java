package com.flowdash.dto;

import java.time.LocalDate;

public record GoalActivityRequest(
        LocalDate activityDate,
        String note
) {
}
