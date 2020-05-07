package com.futurewei.alcor.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.CONFLICT, reason="network range already exists")
public class NetworkRangeExistException extends Exception {
}
