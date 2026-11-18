package com.example.coursewaitlist.service;

public record DropResult(
        boolean dropped,
        String learnerId,
        String promotedLearnerId,
        String message
) { }
