package com.smartsplit.exception;

/** Domain exception for InvalidAmountException. */
public class InvalidAmountException extends ExpenseException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
