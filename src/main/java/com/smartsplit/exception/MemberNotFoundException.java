package com.smartsplit.exception;

/** Domain exception for MemberNotFoundException. */
public class MemberNotFoundException extends ExpenseException {
    public MemberNotFoundException(String message) {
        super(message);
    }
}
