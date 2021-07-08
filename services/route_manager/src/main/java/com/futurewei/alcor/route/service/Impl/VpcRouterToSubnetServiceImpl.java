/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class VpcRouterToSubnetServiceImpl implements VpcRouterToSubnetService {

    @Value("${microservices.subnet.service.url}")
    private String subnetUrl;

    private final RestTemplate restTemplate;

    public VpcRouterToSubnetServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

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
