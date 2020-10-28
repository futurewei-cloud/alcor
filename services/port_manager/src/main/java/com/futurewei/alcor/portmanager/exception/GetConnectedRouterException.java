package com.futurewei.alcor.portmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Get connected routers exception")
public class GetConnectedRouterException extends Exception {
}
