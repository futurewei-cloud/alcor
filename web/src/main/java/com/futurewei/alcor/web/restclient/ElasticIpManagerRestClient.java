package com.futurewei.alcor.web.restclient;

import org.springframework.beans.factory.annotation.Value;

public class ElasticIpManagerRestClient extends AbstractRestClient {
    @Value("${microservices.elasticip.service.url:#{\"\"}}")
    private String elasticIpManagerUrl;



}
