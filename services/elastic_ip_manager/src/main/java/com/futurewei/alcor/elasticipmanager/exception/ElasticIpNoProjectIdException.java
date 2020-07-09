package com.futurewei.alcor.elasticipmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.BAD_REQUEST, reason="Path variable project_id is empty")
public class ElasticIpNoProjectIdException extends Exception {
}
