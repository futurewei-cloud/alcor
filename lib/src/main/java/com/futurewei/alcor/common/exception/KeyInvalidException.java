package com.futurewei.alcor.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.BAD_REQUEST, reason="Key is invalid")
public class KeyInvalidException extends Exception {
}
