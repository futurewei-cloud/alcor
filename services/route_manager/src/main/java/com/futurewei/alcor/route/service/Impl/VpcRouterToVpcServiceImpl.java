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

import com.futurewei.alcor.route.exception.CanNotFindVpc;
import com.futurewei.alcor.route.service.VpcRouterToVpcService;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class VpcRouterToVpcServiceImpl implements VpcRouterToVpcService {

    @Value("${microservices.vpc.service.url}")
    private String vpcUrl;

    private final RestTemplate restTemplate;

    public VpcRouterToVpcServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

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
