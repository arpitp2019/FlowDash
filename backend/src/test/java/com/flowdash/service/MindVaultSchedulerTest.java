package com.flowdash.service;

import com.flowdash.domain.AppUser;
import com.flowdash.domain.AuthProvider;
import com.flowdash.domain.MindVaultItemStatus;
import com.flowdash.domain.MindVaultLearningItem;
import com.flowdash.domain.MindVaultReviewRating;
import com.flowdash.domain.MindVaultSubject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MindVaultSchedulerTest {

    @Test
    void againReviewResetsQueueAndKeepsTopicActive() {
        MindVaultLearningItem item = learningItem(78, 2, 4);

        MindVaultScheduler.MindVaultReviewOutcome outcome = MindVaultScheduler.applyReview(item, MindVaultReviewRating.AGAIN, LocalDate.of(2026, 4, 21));

        assertThat(outcome.status()).isEqualTo(MindVaultItemStatus.ACTIVE);
        assertThat(outcome.reviewStreak()).isZero();
        assertThat(outcome.nextIntervalDays()).isEqualTo(1);
        assertThat(outcome.nextReviewDate()).isEqualTo(LocalDate.of(2026, 4, 21));
        assertThat(outcome.masteryScore()).isLessThan(78);
    }

    @Test
    void easyReviewCanMasterTheTopicAfterEnoughMomentum() {
        MindVaultLearningItem item = learningItem(84, 2, 2);

        MindVaultScheduler.MindVaultReviewOutcome outcome = MindVaultScheduler.applyReview(item, MindVaultReviewRating.EASY, LocalDate.of(2026, 4, 21));

        assertThat(outcome.mastered()).isTrue();
        assertThat(outcome.status()).isEqualTo(MindVaultItemStatus.MASTERED);
        assertThat(outcome.reviewStreak()).isEqualTo(3);
        assertThat(outcome.nextReviewDate()).isAfter(LocalDate.of(2026, 4, 21).plusDays(1));
        assertThat(outcome.masteryScore()).isGreaterThanOrEqualTo(80);
    }

    private static MindVaultLearningItem learningItem(int masteryScore, int reviewStreak, int reviewIntervalDays) {
        AppUser user = user(1L);
        MindVaultSubject subject = new MindVaultSubject(user, "Math", "Study plan", 3, 80, null, null, false);
        subject.setId(10L);
        return new MindVaultLearningItem(
                user,
                subject,
                null,
                null,
                "Topic",
                "Prompt",
                "Answer",
                null,
                null,
                3,
                3,
                masteryScore,
                reviewStreak,
                4,
                3,
                2.1d,
                reviewIntervalDays,
                LocalDate.of(2026, 4, 21),
                null,
                null,
                MindVaultItemStatus.ACTIVE,
                null
        );
    }

    private static AppUser user(Long id) {
        AppUser user = new AppUser("arpit@example.com", "Arpit", null, AuthProvider.LOCAL, null);
        user.setId(id);
        return user;
    }
}
