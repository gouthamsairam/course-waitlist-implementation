package com.example.coursewaitlist.service;

public class UnknownOfferingException extends RuntimeException {
    public UnknownOfferingException(String message) {
        super(message);
    }
}
