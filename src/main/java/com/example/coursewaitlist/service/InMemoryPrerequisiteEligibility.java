package com.example.coursewaitlist.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory prerequisite registry. Learners default to ineligible. */
@Component
public class InMemoryPrerequisiteEligibility {
    private final Map<String, Boolean> eligibilityByLearner = new ConcurrentHashMap<>();

    public void setSatisfied(String learnerId, boolean satisfied) {
        validateLearnerId(learnerId);
        eligibilityByLearner.put(learnerId, satisfied);
    }

    public boolean isSatisfied(String learnerId) {
        return Boolean.TRUE.equals(eligibilityByLearner.get(learnerId));
    }

    private void validateLearnerId(String learnerId) {
        if (learnerId == null || learnerId.isBlank()) {
            throw new IllegalArgumentException("learnerId must not be blank");
        }
    }
}
