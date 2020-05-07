package com.futurewei.alcor.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.NOT_FOUND, reason="vlan is not found")
public class VlanNotFoundException extends Exception{
}
