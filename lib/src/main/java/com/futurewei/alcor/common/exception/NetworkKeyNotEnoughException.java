package com.futurewei.alcor.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.PRECONDITION_FAILED, reason="Key is not enough to be allocated")
public class NetworkKeyNotEnoughException extends Exception {
}
