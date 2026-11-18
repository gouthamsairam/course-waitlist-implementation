package com.example.coursewaitlist.api;

import com.example.coursewaitlist.service.InMemoryPrerequisiteEligibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/learners")
public class PrerequisiteController {
    private final InMemoryPrerequisiteEligibility prerequisiteEligibility;

    public PrerequisiteController(InMemoryPrerequisiteEligibility prerequisiteEligibility) {
        this.prerequisiteEligibility = prerequisiteEligibility;
    }

    @PutMapping("/{learnerId}/prerequisite")
    public PrerequisiteStatusResponse updatePrerequisite(
            @PathVariable @NotBlank String learnerId,
            @Valid @RequestBody PrerequisiteStatusRequest request) {
        prerequisiteEligibility.setSatisfied(learnerId, request.satisfied());
        return new PrerequisiteStatusResponse(learnerId, request.satisfied());
    }

    public record PrerequisiteStatusRequest(boolean satisfied) { }
    public record PrerequisiteStatusResponse(String learnerId, boolean satisfied) { }
}
