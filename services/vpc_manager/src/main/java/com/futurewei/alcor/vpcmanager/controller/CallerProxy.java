package com.futurewei.alcor.vpcmanager.controller;

import feign.RequestLine;


public interface CallerProxy {

    @RequestLine("GET /vpcs")
    Caller greeting();


}