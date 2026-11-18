package com.example.coursewaitlist.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Thread-safe in-memory implementation for one configured course offering. */
@Service
public class InMemoryOfferingEnrollmentService implements OfferingEnrollmentService {
    private final String offeringId;
    private final int capacity;
    private final InMemoryPrerequisiteEligibility prerequisiteEligibility;
    private final Set<String> enrolledLearners = new LinkedHashSet<>();
    private final Deque<String> waitlistedLearners = new ArrayDeque<>();

    public InMemoryOfferingEnrollmentService(
            InMemoryPrerequisiteEligibility prerequisiteEligibility,
            @Value("${waitlist.offering-id:COURSE-101}") String offeringId,
            @Value("${waitlist.capacity:2}") int capacity) {
        if (offeringId == null || offeringId.isBlank()) {
            throw new IllegalArgumentException("offeringId must not be blank");
        }
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be greater than zero");
        }
        this.prerequisiteEligibility = prerequisiteEligibility;
        this.offeringId = offeringId;
        this.capacity = capacity;
    }

    @Override
    public synchronized OfferingSnapshot getSnapshot(String requestedOfferingId) {
        validateOffering(requestedOfferingId);
        return new OfferingSnapshot(offeringId, capacity, enrolledLearners.size(),
                capacity - enrolledLearners.size(), new ArrayList<>(enrolledLearners),
                new ArrayList<>(waitlistedLearners));
    }

    @Override
    public synchronized EnrollmentStatus enroll(String requestedOfferingId, String learnerId) {
        validateOffering(requestedOfferingId);
        validateLearner(learnerId);

        if (enrolledLearners.contains(learnerId)) return EnrollmentStatus.ALREADY_ENROLLED;
        if (waitlistedLearners.contains(learnerId)) return EnrollmentStatus.ALREADY_WAITLISTED;
        if (!prerequisiteEligibility.isSatisfied(learnerId)) return EnrollmentStatus.INELIGIBLE;

        if (enrolledLearners.size() < capacity) {
            enrolledLearners.add(learnerId);
            return EnrollmentStatus.ENROLLED;
        }

        waitlistedLearners.addLast(learnerId);
        return EnrollmentStatus.WAITLISTED;
    }

    @Override
    public synchronized DropResult drop(String requestedOfferingId, String learnerId) {
        validateOffering(requestedOfferingId);
        validateLearner(learnerId);

        if (!enrolledLearners.remove(learnerId)) {
            return new DropResult(false, learnerId, null,
                    "Learner is not enrolled; no enrollment was dropped.");
        }

        String promotedLearnerId = promoteNextEligibleLearner();
        String message = promotedLearnerId == null
                ? "Enrollment dropped; no eligible waitlisted learner was available for promotion."
                : "Enrollment dropped and the earliest eligible waitlisted learner was promoted.";
        return new DropResult(true, learnerId, promotedLearnerId, message);
    }

    /** Skips ineligible entries for this promotion while retaining their FIFO order. */
    private String promoteNextEligibleLearner() {
        List<String> skippedIneligible = new ArrayList<>();
        String promoted = null;

        while (!waitlistedLearners.isEmpty()) {
            String candidate = waitlistedLearners.removeFirst();
            if (prerequisiteEligibility.isSatisfied(candidate)) {
                enrolledLearners.add(candidate);
                promoted = candidate;
                break;
            }
            skippedIneligible.add(candidate);
        }

        // Restore skipped entries at the head, preserving their original order.
        for (int i = skippedIneligible.size() - 1; i >= 0; i--) {
            waitlistedLearners.addFirst(skippedIneligible.get(i));
        }
        return promoted;
    }

    private void validateOffering(String requestedOfferingId) {
        if (!offeringId.equals(requestedOfferingId)) {
            throw new UnknownOfferingException("Unknown offering: " + requestedOfferingId);
        }
    }

    private void validateLearner(String learnerId) {
        if (learnerId == null || learnerId.isBlank()) {
            throw new IllegalArgumentException("learnerId must not be blank");
        }
    }
}
