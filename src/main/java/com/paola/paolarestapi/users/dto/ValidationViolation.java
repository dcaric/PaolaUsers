package com.paola.paolarestapi.users.dto;

public class ValidationViolation {
    private String field;
    private String rule;
    private String detail;

    public ValidationViolation() {
    }

    public ValidationViolation(String field, String rule, String detail) {
        this.field = field;
        this.rule = rule;
        this.detail = detail;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}

