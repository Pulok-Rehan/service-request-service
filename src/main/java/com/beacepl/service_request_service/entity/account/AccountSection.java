package com.beacepl.service_request_service.entity.account;

public enum AccountSection {
    ACCOUNT_TYPE("Account Type", 1),
    LIVE_VALIDATION("Live Validation", 2),
    PERSONAL_DETAILS("Personal Details", 3),
    NOMINEE_DETAILS("Nominee Details", 4),
    BANK_DETAILS("Bank Details", 5),
    DOCUMENTS("Documents", 6);

    private final String displayName;
    private final int stepNumber;

    AccountSection(String displayName, int stepNumber) {
        this.displayName = displayName;
        this.stepNumber = stepNumber;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getStepNumber() {
        return stepNumber;
    }
}