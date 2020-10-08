package com.futurewei.alcor.networkaclmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.PRECONDITION_FAILED, reason="Action invalid")
public class ActionInvalid extends Exception {
}
