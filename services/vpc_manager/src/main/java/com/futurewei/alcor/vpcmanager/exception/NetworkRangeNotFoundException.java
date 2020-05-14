package com.futurewei.alcor.vpcmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.NOT_FOUND, reason="Network range not Found")
public class NetworkRangeNotFoundException extends Exception{
}
