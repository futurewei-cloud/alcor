package com.futurewei.alcor.elasticipmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.NOT_FOUND, reason="The elastic ip range is not founded ")
public class ElasticIpRangeNotFoundException extends Exception {
}
