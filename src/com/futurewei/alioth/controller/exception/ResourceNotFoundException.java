package com.futurewei.alioth.controller.exception;

public class ResourceNotFoundException extends Exception {

    public ResourceNotFoundException(){

    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
    //TODO: improve logging
}
