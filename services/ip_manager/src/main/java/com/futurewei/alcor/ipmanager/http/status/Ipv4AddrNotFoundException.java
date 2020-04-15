package com.futurewei.alcor.ipmanager.http.status;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.NOT_FOUND, reason="Ipv4 address not found")
public class Ipv4AddrNotFoundException extends Exception {
}
