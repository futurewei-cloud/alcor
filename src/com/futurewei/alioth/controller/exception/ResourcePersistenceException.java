package com.futurewei.alioth.controller.exception;

public class ResourcePersistenceException extends Exception {

    public ResourcePersistenceException(){

    }

    public ResourcePersistenceException(String message) {
        super(message);
    }

    //TODO: improve logging
}
