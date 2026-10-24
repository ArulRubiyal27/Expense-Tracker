package com.expensetracker.Exception;

public class ResourcenotFoundException extends RuntimeException {
    public ResourcenotFoundException(String message) {
        super(message);
    }
}
