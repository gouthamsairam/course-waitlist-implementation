package com.example.coursewaitlist.api;

import com.example.coursewaitlist.service.DropResult;
import com.example.coursewaitlist.service.EnrollmentStatus;
import com.example.coursewaitlist.service.OfferingEnrollmentService;
import com.example.coursewaitlist.service.OfferingSnapshot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class EnrollmentController {
    private final OfferingEnrollmentService enrollmentService;

    public EnrollmentController(OfferingEnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/offerings/{offeringId}")
    public OfferingSnapshot getOffering(@PathVariable String offeringId) {
        return enrollmentService.getSnapshot(offeringId);
    }

    @PostMapping("/offerings/{offeringId}/enrollments")
    public EnrollmentResponse enroll(@PathVariable String offeringId,
                                     @Valid @RequestBody EnrollmentRequest request) {
        EnrollmentStatus status = enrollmentService.enroll(offeringId, request.learnerId());
        return new EnrollmentResponse(status, request.learnerId());
    }

    @DeleteMapping("/offerings/{offeringId}/enrollments/{learnerId}")
    public DropResult drop(@PathVariable String offeringId, @PathVariable String learnerId) {
        return enrollmentService.drop(offeringId, learnerId);
    }

    public record EnrollmentRequest(@NotBlank String learnerId) { }
    public record EnrollmentResponse(EnrollmentStatus status, String learnerId) { }
}
