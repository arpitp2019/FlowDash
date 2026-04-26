package com.flowdash.service;

import com.flowdash.domain.MindVaultItemStatus;
import com.flowdash.domain.MindVaultLearningItem;
import com.flowdash.domain.MindVaultReviewRating;

import java.time.LocalDate;

public final class MindVaultScheduler {

    private static final double MIN_EASE = 1.3d;
    private static final double MAX_EASE = 2.8d;
    private static final int MIN_MASTERED_STREAK = 3;

    private MindVaultScheduler() {
    }

    public static MindVaultReviewOutcome applyReview(MindVaultLearningItem item, MindVaultReviewRating rating, LocalDate today) {
        int previousIntervalDays = Math.max(1, valueOrDefault(item.getReviewIntervalDays(), 1));
        int mastery = clamp(valueOrDefault(item.getMasteryScore(), 15) + masteryDelta(rating), 0, 100);
        int difficulty = clamp(valueOrDefault(item.getDifficulty(), 3) + difficultyDelta(rating), 1, 5);
        int reviewStreak = rating.getValue() >= 2 ? valueOrDefault(item.getReviewStreak(), 0) + 1 : 0;
        int reviewCount = valueOrDefault(item.getReviewCount(), 0) + 1;
        int successCount = valueOrDefault(item.getSuccessCount(), 0) + (rating.getValue() >= 2 ? 1 : 0);
        int lapseCount = valueOrDefault(item.getLapseCount(), 0) + (rating == MindVaultReviewRating.AGAIN ? 1 : 0);
        double easeFactor = clamp(valueOrDefault(item.getEaseFactor(), 2.1d) + easeAdjustment(rating), MIN_EASE, MAX_EASE);
        int nextIntervalDays = nextInterval(previousIntervalDays, easeFactor, rating);

        boolean mastered = mastery >= targetMastery(item) && reviewStreak >= MIN_MASTERED_STREAK;
        MindVaultItemStatus status = mastered ? MindVaultItemStatus.MASTERED : MindVaultItemStatus.ACTIVE;
        LocalDate nextReviewDate = switch (rating) {
            case AGAIN, HARD -> today.plusDays(1);
            case GOOD, EASY -> today.plusDays(mastered ? Math.max(7, nextIntervalDays) : nextIntervalDays);
        };

        return new MindVaultReviewOutcome(
                mastery,
                difficulty,
                reviewStreak,
                reviewCount,
                successCount,
                lapseCount,
                easeFactor,
                nextIntervalDays,
                status,
                nextReviewDate,
                mastered,
                rating.getValue(),
                previousIntervalDays
        );
    }

    private static int targetMastery(MindVaultLearningItem item) {
        if (item.getSubject() != null && item.getSubject().getTargetMastery() != null) {
            return item.getSubject().getTargetMastery();
        }
        return 85;
    }

    private static int nextInterval(int previousIntervalDays, double easeFactor, MindVaultReviewRating rating) {
        return switch (rating) {
            case AGAIN, HARD -> 1;
            case GOOD -> Math.max(2, (int) Math.round(previousIntervalDays * easeFactor));
            case EASY -> Math.max(3, (int) Math.round(previousIntervalDays * (easeFactor + 0.35d)));
        };
    }

    private static int masteryDelta(MindVaultReviewRating rating) {
        return switch (rating) {
            case AGAIN -> -15;
            case HARD -> -6;
            case GOOD -> 8;
            case EASY -> 12;
        };
    }

    private static int difficultyDelta(MindVaultReviewRating rating) {
        return switch (rating) {
            case AGAIN -> 1;
            case HARD -> 1;
            case GOOD -> 0;
            case EASY -> -1;
        };
    }

    private static double easeAdjustment(MindVaultReviewRating rating) {
        return switch (rating) {
            case AGAIN -> -0.20d;
            case HARD -> -0.08d;
            case GOOD -> 0.04d;
            case EASY -> 0.12d;
        };
    }

    private static int valueOrDefault(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static double valueOrDefault(Double value, double defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public record MindVaultReviewOutcome(
            int masteryScore,
            int difficulty,
            int reviewStreak,
            int reviewCount,
            int successCount,
            int lapseCount,
            double easeFactor,
            int nextIntervalDays,
            MindVaultItemStatus status,
            LocalDate nextReviewDate,
            boolean mastered,
            int rating,
            int previousIntervalDays
    ) {
    }
}
