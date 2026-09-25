package com.idoceb00.laruta.backend.exception;

// If a search is launched specifically by ID, any other type of search would not constitute an exceptional error
public class BarNotFoundException extends RuntimeException {
    public BarNotFoundException(String message) {
        super(message);
    }
}
