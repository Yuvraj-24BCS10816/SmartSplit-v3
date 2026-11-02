package com.smartsplit.exception;

/** Wraps persistence failures with a user-facing domain message. */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(String message) {
        super(message);
    }
}
