package com.futurewei.alcor.ipmanager.http.status;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.PRECONDITION_FAILED, reason="Ipv4 address number not enough in the specified Ipv4AddrRange")
public class Ipv4AddrNotEnoughException extends Exception {
}
