package com.flowdash.dto;

import java.time.LocalDate;

public record MindVaultForecastPointResponse(
        LocalDate date,
        long count
) {
}
