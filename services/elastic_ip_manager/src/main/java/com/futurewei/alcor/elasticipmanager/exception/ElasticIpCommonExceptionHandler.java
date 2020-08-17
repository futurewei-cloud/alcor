/*
Copyright 2020 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/

package com.futurewei.alcor.elasticipmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.Serializable;


@RestControllerAdvice
public class ElasticIpCommonExceptionHandler {

    public static class Result implements Serializable {

        private String msg;

        public Result(String msg) {
            this.msg = msg;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "msg='" + msg + '\'' +
                    '}';
        }
    }

    @ExceptionHandler(value = ElasticIpCommonException.class)
    public ResponseEntity<Result> serviceException(ElasticIpCommonException e) {
        String message = e.getMessage();
        if(message == null) {
            message = "Elastic ip exception occurs";
        }
        HttpStatus status = e.getHttpStatus();
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity<>(new Result(message), status);
    }
}
