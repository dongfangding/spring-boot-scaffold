package com.ddf.scaffold.fw.security;

public class AuthenticationException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 5921703741715981322L;

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        cause.printStackTrace();
    }
}
