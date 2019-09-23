package com.futurewei.alcor.controller.exception;

public class ResourcePreExistenceException extends Exception {

    public ResourcePreExistenceException(){

    }

    public ResourcePreExistenceException(String message) {
        super(message);
    }

    //TODO: improve logging
}
