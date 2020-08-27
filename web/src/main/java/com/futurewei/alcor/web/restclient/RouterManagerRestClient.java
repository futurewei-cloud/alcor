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
package com.futurewei.alcor.web.restclient;

import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.router.RouterSubnets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouterManagerRestClient extends AbstractRestClient {
    @Value("${microservices.router.service.url:#{\"\"}}")
    private String routerManagerUrl;

    public RouterSubnets getRouterSubnets(String projectId, String vpcId, String subnetId) throws Exception {
        String url = String.format("/project/%s/vpcs/%s/subnets/%s/connected-subnets", projectId, vpcId, subnetId);
        RouterSubnets routerSubnets = restTemplate.getForObject(url, RouterSubnets.class);
        if (routerSubnets == null) {
            throw new Exception("Get router connected subnets failed");
        }

        return routerSubnets;
    }

    public RouterSubnets getConnectedSubnetIdsBulk(String projectId, String vpcId, String subnetId) throws Exception {
        String url = String.format("/project/%s/vpcs/%s/subnets/%s/connected-subnets", projectId, vpcId, subnetId);
        RouterSubnets routerSubnets = restTemplate.getForObject(url, RouterSubnets.class);
        if (routerSubnets == null) {
            throw new Exception("Get vpc connected subnets failed");
        }

        return routerSubnets;
    }

    public void addRouterInterface(String routerId, PortEntity portEntity) {
        String url = String.format("/v2.0/routers/%s/add_router_interface", routerId);
    }

    public void removeRouterInterface(String routerId) {
        String url = String.format("/v2.0/routers/%s/remove_router_interface", routerId);
    }
}
