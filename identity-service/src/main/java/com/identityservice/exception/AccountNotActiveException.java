package com.identityservice.exception;

public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException() {
        super("Your account is not active. Please contact support.");
    }
}
