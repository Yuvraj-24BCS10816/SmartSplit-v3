package com.smartsplit.exception;

/** Domain exception for GroupNotFoundException. */
public class GroupNotFoundException extends ExpenseException {
    public GroupNotFoundException(String message) {
        super(message);
    }
}
