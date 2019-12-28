package com.futurewei.alcor.controller.exception;

public class ParameterUnexpectedValueException extends Exception {

    public ParameterUnexpectedValueException() {

    }

    public ParameterUnexpectedValueException(String message) {
        super(message);
    }
    //TODO: improve logging

}
