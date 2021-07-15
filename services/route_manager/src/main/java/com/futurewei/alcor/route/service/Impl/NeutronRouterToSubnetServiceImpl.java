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

import com.futurewei.alcor.route.service.NeutronRouterToSubnetService;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.entity.subnet.SubnetsWebJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NeutronRouterToSubnetServiceImpl implements NeutronRouterToSubnetService {

    @Value("${microservices.subnet.service.url}")
    private String subnetUrl;

    private final RestTemplate restTemplate;

    public NeutronRouterToSubnetServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public SubnetWebJson getSubnet(String projectid, String subnetId) {
        String subnetManagerServiceUrl = subnetUrl+ "/project/" + projectid + "/subnets/" + subnetId;
        SubnetWebJson response = restTemplate.getForObject(subnetManagerServiceUrl, SubnetWebJson.class);
        return response;
    }

    @Override
    public SubnetsWebJson getSubnetsByPortId(String projectid, String portId) {
        String subnetManagerServiceUrl = subnetUrl + "/project/" + projectid + "/subnets?gatewayPortId=" + portId;
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
