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

import com.futurewei.alcor.route.exception.CanNotFindSubnet;
import com.futurewei.alcor.route.service.VpcRouterToSubnetService;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
}
