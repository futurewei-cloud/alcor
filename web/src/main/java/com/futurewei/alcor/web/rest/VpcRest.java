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
package com.futurewei.alcor.web.rest;

import com.futurewei.alcor.web.entity.vpc.VpcStateJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VpcRest extends AbstractRest {
    @Value("${microservices.vpc.service.url:#{\"\"}}")
    private String vpcManagerUrl;

    public VpcStateJson verifyVpc(String projectId, String vpcId) throws Exception {
        String url = vpcManagerUrl + "/project/" + projectId + "/vpcs/" + vpcId;
        VpcStateJson vpcState = restTemplate.getForObject(url, VpcStateJson.class);
        if (vpcState == null) {
            throw new Exception("verify vpc failed");
        }

        return vpcState;
    }
}
