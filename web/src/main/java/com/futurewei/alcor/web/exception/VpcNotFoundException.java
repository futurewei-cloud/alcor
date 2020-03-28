package com.futurewei.alcor.web.exception;

import com.futurewei.alcor.common.exception.ResourceNotFoundException;

public class VpcNotFoundException extends ResourceNotFoundException {

    public VpcNotFoundException() {
    }

    public VpcNotFoundException(String message) {
        super(message);
    }

    public VpcNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public VpcNotFoundException(Throwable cause) {
        super(cause);
    }
}
