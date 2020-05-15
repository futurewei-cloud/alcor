package com.futurewei.alcor.vpcmanager.service.Impl;

import com.futurewei.alcor.vpcmanager.service.VpcService;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcWebResponseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class VpcServiceImpl implements VpcService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${microservices.route.service.url}")
    private String routeUrl;

    private RestTemplate restTemplate = new RestTemplate();

    /**
     * Get route rule info
     * @param vpcId
     * @param vpcState
     * @return route state
     */
    @Override
    public RouteWebJson getRoute(String vpcId, VpcWebResponseObject vpcState) {
        String routeManagerServiceUrl = routeUrl + vpcId + "/routes";
        HttpEntity<VpcWebJson> request = new HttpEntity<>(new VpcWebJson(vpcState));
        RouteWebJson response = restTemplate.postForObject(routeManagerServiceUrl, request, RouteWebJson.class);
        return response;
    }
}
