package com.flowdash.domain;

public enum MindVaultReviewRating {
    AGAIN(0),
    HARD(1),
    GOOD(2),
    EASY(3);

    private final int value;

    MindVaultReviewRating(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MindVaultReviewRating fromValue(int value) {
        for (MindVaultReviewRating rating : values()) {
            if (rating.value == value) {
                return rating;
            }
        }
        throw new IllegalArgumentException("Invalid review rating");
    }
}
