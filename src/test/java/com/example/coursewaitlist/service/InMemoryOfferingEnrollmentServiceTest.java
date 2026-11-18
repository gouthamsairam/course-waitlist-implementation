package com.example.coursewaitlist.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryOfferingEnrollmentServiceTest {
    private InMemoryPrerequisiteEligibility eligibility;
    private InMemoryOfferingEnrollmentService service;

    @BeforeEach
    void setUp() {
        eligibility = new InMemoryPrerequisiteEligibility();
        service = new InMemoryOfferingEnrollmentService(eligibility, "JAVA-101", 2);
        for (String learner : List.of("A", "B", "C", "D", "E")) eligibility.setSatisfied(learner, true);
    }

    @Test void enrollsWhenCapacityIsAvailable() {
        assertEquals(EnrollmentStatus.ENROLLED, service.enroll("JAVA-101", "A"));
        assertEquals(1, service.getSnapshot("JAVA-101").enrolledCount());
    }

    @Test void queuesLearnerWhenOfferingIsFull() {
        service.enroll("JAVA-101", "A"); service.enroll("JAVA-101", "B");
        assertEquals(EnrollmentStatus.WAITLISTED, service.enroll("JAVA-101", "C"));
    }

    @Test void duplicateEnrollAndWaitlistRequestsDoNotDuplicateRecords() {
        service.enroll("JAVA-101", "A");
        assertEquals(EnrollmentStatus.ALREADY_ENROLLED, service.enroll("JAVA-101", "A"));
        service.enroll("JAVA-101", "B"); service.enroll("JAVA-101", "C");
        assertEquals(EnrollmentStatus.ALREADY_WAITLISTED, service.enroll("JAVA-101", "C"));
        assertEquals(List.of("C"), service.getSnapshot("JAVA-101").waitlistedLearners());
    }

    @Test void promotesEarliestEligibleLearnerInFifoOrder() {
        service.enroll("JAVA-101", "A"); service.enroll("JAVA-101", "B");
        service.enroll("JAVA-101", "C"); service.enroll("JAVA-101", "D");
        DropResult result = service.drop("JAVA-101", "A");
        assertTrue(result.dropped());
        assertEquals("C", result.promotedLearnerId());
        assertEquals(List.of("D"), service.getSnapshot("JAVA-101").waitlistedLearners());
    }

    @Test void rejectsLearnerWhoHasNotSatisfiedPrerequisite() {
        eligibility.setSatisfied("X", false);
        assertEquals(EnrollmentStatus.INELIGIBLE, service.enroll("JAVA-101", "X"));
        assertFalse(service.getSnapshot("JAVA-101").enrolledLearners().contains("X"));
    }

    @Test void skipsButRetainsIneligibleWaitlistedLearnerDuringPromotion() {
        service.enroll("JAVA-101", "A"); service.enroll("JAVA-101", "B");
        service.enroll("JAVA-101", "C"); service.enroll("JAVA-101", "D");
        eligibility.setSatisfied("C", false);
        DropResult result = service.drop("JAVA-101", "A");
        assertEquals("D", result.promotedLearnerId());
        assertEquals(List.of("C"), service.getSnapshot("JAVA-101").waitlistedLearners());
    }

    @Test void repeatedDropDoesNotCorruptState() {
        service.enroll("JAVA-101", "A");
        assertTrue(service.drop("JAVA-101", "A").dropped());
        assertFalse(service.drop("JAVA-101", "A").dropped());
        assertEquals(0, service.getSnapshot("JAVA-101").enrolledCount());
    }

    @Test void rejectsUnknownOfferingAndBlankLearner() {
        assertThrows(UnknownOfferingException.class, () -> service.getSnapshot("OTHER"));
        assertThrows(IllegalArgumentException.class, () -> service.enroll("JAVA-101", " "));
    }

    @Test void concurrentEnrollmentsNeverExceedCapacity() throws Exception {
        var pool = Executors.newFixedThreadPool(8);
        try {
            var tasks = IntStream.range(0, 12).<Callable<EnrollmentStatus>>mapToObj(i -> {
                String learner = "L" + i;
                eligibility.setSatisfied(learner, true);
                return () -> service.enroll("JAVA-101", learner);
            }).toList();
            pool.invokeAll(tasks);
            OfferingSnapshot snapshot = service.getSnapshot("JAVA-101");
            assertEquals(2, snapshot.enrolledCount());
            assertEquals(10, snapshot.waitlistedLearners().size());
        } finally { pool.shutdownNow(); }
    }
}
