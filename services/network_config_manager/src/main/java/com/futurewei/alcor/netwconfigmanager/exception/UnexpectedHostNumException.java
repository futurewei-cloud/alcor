package com.futurewei.alcor.netwconfigmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Expect one host but find more than one")
public class UnexpectedHostNumException extends Exception {
}
