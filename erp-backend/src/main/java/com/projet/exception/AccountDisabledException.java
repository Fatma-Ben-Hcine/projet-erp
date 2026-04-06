package com.projet.exception;

import org.springframework.security.authentication.DisabledException;

public class AccountDisabledException extends DisabledException {
    public AccountDisabledException(String message) {
        super(message);
    }
}
