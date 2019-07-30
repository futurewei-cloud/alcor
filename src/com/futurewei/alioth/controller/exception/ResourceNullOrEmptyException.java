package com.futurewei.alioth.controller.exception;

public class ResourceNullOrEmptyException extends Exception {

    public ResourceNullOrEmptyException(String message) {
        super(message);
    }

    //TODO: improve logging
}
