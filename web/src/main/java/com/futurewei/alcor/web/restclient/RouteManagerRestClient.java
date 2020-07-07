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

import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.route.RoutesWebJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteManagerRestClient extends AbstractRestClient {
    @Value("${microservices.route.service.url:#{\"\"}}")
    private String routeManagerUrl;

    public RoutesWebJson getRouteBySubnetId(String subnetId) throws Exception {
        String url = routeManagerUrl + "/subnets/" + subnetId + "/get";
        RoutesWebJson routesWebJson = restTemplate.getForObject(url, RoutesWebJson.class);
        if (routesWebJson == null) {
            throw new Exception("Get route by subnet id failed");
        }

        return routesWebJson;
    }
}
