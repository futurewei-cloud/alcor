package com.futurewei.alcor.vpcmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.NOT_FOUND, reason="Vlan range not Found")
public class VlanRangeNotFoundException extends Exception {
}
