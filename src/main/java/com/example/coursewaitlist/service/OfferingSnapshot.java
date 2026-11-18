package com.example.coursewaitlist.service;

import java.util.List;

public record OfferingSnapshot(
        String offeringId,
        int capacity,
        int enrolledCount,
        int availableSeats,
        List<String> enrolledLearners,
        List<String> waitlistedLearners
) {
    public OfferingSnapshot {
        enrolledLearners = List.copyOf(enrolledLearners);
        waitlistedLearners = List.copyOf(waitlistedLearners);
    }
}
