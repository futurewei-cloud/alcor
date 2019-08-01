package com.futurewei.alioth.controller.exception;

public class ParameterNullOrEmptyException extends Exception {

    public ParameterNullOrEmptyException(String message) {
        super(message);
    }

    //TODO: improve logging
}
