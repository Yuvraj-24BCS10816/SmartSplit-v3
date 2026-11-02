package com.smartsplit.exception;

/** Base class for expected expense-management validation and domain failures. */
public class ExpenseException extends RuntimeException {
    public ExpenseException(String message) {
        super(message);
    }

    public ExpenseException(String message, Throwable cause) {
        super(message, cause);
    }
}
