package com.futurewei.alcor.dataplane.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.PRECONDITION_FAILED, reason = "DPM path mode invalid")
public class InvalidPathModeException extends Exception{

}
