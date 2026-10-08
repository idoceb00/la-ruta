package com.idoceb00.laruta.backend.exception;

public class MembershipAlreadyExistsException extends RuntimeException {
    public MembershipAlreadyExistsException(String message) {
        super(message);
    }
}
