package com.iamkaf.bonded.api.augment;

/**
 * Result of an augment progress mutation.
 */
public record AugmentProgressResult(
        Status status,
        Augment augment,
        int previousProgress,
        int currentProgress
) {
    public boolean changed() {
        return previousProgress != currentProgress;
    }

    public boolean active() {
        return currentProgress >= augment.activationProgress();
    }

    public enum Status {
        INELIGIBLE,
        UNCHANGED,
        PROGRESSED,
        ACTIVATED,
        DEACTIVATED
    }
}
