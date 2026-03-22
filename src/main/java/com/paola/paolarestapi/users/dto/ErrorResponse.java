package com.paola.paolarestapi.users.dto;

import java.util.List;

public class ErrorResponse {
    private String message;
    private List<ValidationViolation> violations;

    public ErrorResponse() {
    }

    public ErrorResponse(String message, List<ValidationViolation> violations) {
        this.message = message;
        this.violations = violations;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ValidationViolation> getViolations() {
        return violations;
    }

    public void setViolations(List<ValidationViolation> violations) {
        this.violations = violations;
    }
}

