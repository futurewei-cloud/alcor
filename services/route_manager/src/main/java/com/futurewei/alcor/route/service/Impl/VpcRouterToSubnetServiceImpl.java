/*
Copyright 2019 The Alcor Authors.

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
package com.futurewei.alcor.route.service.Impl;

import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.route.exception.CanNotFindSubnet;
import com.futurewei.alcor.route.service.VpcRouterToSubnetService;
import com.futurewei.alcor.web.entity.route.RouteTableWebJson;
import com.futurewei.alcor.web.entity.subnet.HostRoute;
import com.futurewei.alcor.web.entity.subnet.NewHostRoutes;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.entity.subnet.SubnetsWebJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class VpcRouterToSubnetServiceImpl implements VpcRouterToSubnetService {

    @Value("${microservices.subnet.service.url}")
    private String subnetUrl;

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public SubnetWebJson getSubnet(String projectId, String subnetId) throws CanNotFindSubnet {
        String subnetManagerServiceUrl = subnetUrl+ "/project/" + projectId + "/subnets/" + subnetId;
        SubnetWebJson response = restTemplate.getForObject(subnetManagerServiceUrl, SubnetWebJson.class);
        if (response == null || response.getSubnet() == null) {
            throw new CanNotFindSubnet();
        }
        return response;
    }

    @Override
    public SubnetsWebJson getSubnetsByVpcId(String projectId, String vpcId) {
        String subnetManagerServiceUrl = subnetUrl+ "/project/" + projectId + "/subnets?network_id=" + vpcId;
        SubnetsWebJson response = restTemplate.getForObject(subnetManagerServiceUrl, SubnetsWebJson.class);
        return response;
    }

    @Override
    public void updateRoutingRuleInSubnetManager(String projectId, String subnetId, List<HostRoute> hostRouteToSubnet) {
        String subnetManagerServiceUrl = subnetUrl+ "/project/" + projectId + "/subnets/" + subnetId + "/update_routes";
        HttpEntity<NewHostRoutes> routeRequest = new HttpEntity<>(new NewHostRoutes(hostRouteToSubnet));
        restTemplate.put(subnetManagerServiceUrl, routeRequest, ResponseId.class);
    }
}
