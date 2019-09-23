package com.futurewei.alcor.controller.exception;

public class ParameterNullOrEmptyException extends Exception {

    public ParameterNullOrEmptyException(String message) {
        super(message);
    }

    //TODO: improve logging
}
