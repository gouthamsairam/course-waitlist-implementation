package com.example.coursewaitlist.service;

public interface OfferingEnrollmentService {
    OfferingSnapshot getSnapshot(String offeringId);
    EnrollmentStatus enroll(String offeringId, String learnerId);
    DropResult drop(String offeringId, String learnerId);
}
