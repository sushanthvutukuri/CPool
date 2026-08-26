package com.utd.cpool.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message)
    {
        super(message);
    }    
}
