package com.futurewei.alcor.portmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.INTERNAL_SERVER_ERROR, reason="Update port ipaddress error")
public class UpdatePortIpException extends Exception{}
