package com.futurewei.alcor.elasticipmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.CONFLICT, reason="The elastic ip range has already exist")
public class ElasticIpRangeExistsException extends Exception {
}
