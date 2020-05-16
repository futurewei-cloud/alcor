package com.futurewei.alcor.vpcmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.NOT_FOUND, reason="Key allocation is not found")
public class NetworkKeyAllocNotFoundException extends Exception{
}
