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

import com.futurewei.alcor.route.exception.CanNotFindVpc;
import com.futurewei.alcor.route.service.VpcRouterToVpcService;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class VpcRouterToVpcServiceImpl implements VpcRouterToVpcService {

    @Value("${microservices.vpc.service.url}")
    private String vpcUrl;

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public VpcWebJson getVpcWebJson(String projectId, String vpcId) throws CanNotFindVpc {
        String vpcManagerServiceUrl = vpcUrl + "/project/" + projectId + "/vpcs/" + vpcId;
        VpcWebJson vpcResponse = restTemplate.getForObject(vpcManagerServiceUrl, VpcWebJson.class);
        if (vpcResponse.getNetwork() == null) {
            throw new CanNotFindVpc();
        }
        return vpcResponse;
    }

}
