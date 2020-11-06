package com.futurewei.alcor.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "MAC address format is not valid")
public class MacAddressInvalidException extends Exception{
    public MacAddressInvalidException() {
        super("MAC address format is not valid");
    }

    public MacAddressInvalidException(String message) {
        super(message);
    }

    public MacAddressInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public MacAddressInvalidException(Throwable cause) {
        super(cause);
    }
}
