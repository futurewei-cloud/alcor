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

import com.futurewei.alcor.route.service.NeutronRouterToSubnetService;
import com.futurewei.alcor.route.service.RouterDatabaseService;
import com.futurewei.alcor.route.service.RouterExtraAttributeDatabaseService;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.entity.subnet.SubnetsWebJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NeutronRouterToSubnetServiceImpl implements NeutronRouterToSubnetService {

    @Value("${microservices.subnet.service.url}")
    private String subnetUrl;

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public SubnetWebJson getSubnet(String projectid, String subnetId) {
        String subnetManagerServiceUrl = subnetUrl+ "/project/" + projectid + "/subnets/" + subnetId;
        SubnetWebJson response = restTemplate.getForObject(subnetManagerServiceUrl, SubnetWebJson.class);
        return response;
    }

    @Override
    public SubnetsWebJson getSubnetsByPortId(String projectid, String portId) {
        String subnetManagerServiceUrl = subnetUrl + "/project/" + projectid + "/subnets?port_id=" + portId;
        SubnetsWebJson response = restTemplate.getForObject(subnetManagerServiceUrl, SubnetsWebJson.class);
        return response;
    }

    @Override
    public void updateSubnet(String projectid, String subnetId, SubnetEntity subnet) {
        String subnetManagerServiceUrl = subnetUrl + "/project/" + projectid + "/subnets/" + subnetId;
        HttpEntity<SubnetWebJson> request = new HttpEntity<>(new SubnetWebJson(subnet));
        restTemplate.put(subnetManagerServiceUrl, request, SubnetWebJson.class);
    }

}
