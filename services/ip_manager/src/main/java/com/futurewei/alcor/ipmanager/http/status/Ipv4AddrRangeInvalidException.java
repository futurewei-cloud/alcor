package com.futurewei.alcor.ipmanager.http.status;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.BAD_REQUEST, reason="Ipv4 address range invalid")
public class Ipv4AddrRangeInvalidException extends Exception {
}
