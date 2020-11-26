package com.futurewei.alcor.dataplane.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Pulsar Topic not found")
public class TopicParseFailureException extends Exception{
    public TopicParseFailureException(String message) {
        super(message);
    }
}
