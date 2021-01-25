package com.futurewei.alcor.gatewaymanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GatewayEntityNotFoundException extends Exception{
    public GatewayEntityNotFoundException() {
        super("GatewayEntity could not be found");
    }
}
