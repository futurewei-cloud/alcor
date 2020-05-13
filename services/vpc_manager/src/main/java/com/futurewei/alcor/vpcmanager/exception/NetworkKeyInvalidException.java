package com.futurewei.alcor.vpcmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.BAD_REQUEST, reason="Key is invalid")
public class NetworkKeyInvalidException extends Exception {
}
