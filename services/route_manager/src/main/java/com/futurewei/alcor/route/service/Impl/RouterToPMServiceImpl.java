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

import com.futurewei.alcor.route.exception.PortWebBulkJsonOrPortEntitiesListIsNull;
import com.futurewei.alcor.route.service.RouterToPMService;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.PortWebBulkJson;
import com.futurewei.alcor.web.entity.port.PortWebJson;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.route.RouterUpdateInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouterToPMServiceImpl implements RouterToPMService {

    @Value("${microservices.port.service.url}")
    private String portUrl;

    private final RestTemplate restTemplate;

    public RouterToPMServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public List<String> getSubnetIdsFromPM(String projectid, List<String> gatewayPorts) throws PortWebBulkJsonOrPortEntitiesListIsNull {
        if (gatewayPorts == null) {
            return null;
        }

        List<String> subnetIds = new ArrayList<>();
        String portManagerServiceUrl = portUrl + "/project/" + projectid + "/ports?id=";
        for (int i = 0 ; i < gatewayPorts.size(); i ++) {
            String gatewayPortId = gatewayPorts.get(i);
            if (i != gatewayPorts.size() - 1) {
                portManagerServiceUrl = portManagerServiceUrl + gatewayPortId + ",";
            } else {
                portManagerServiceUrl = portManagerServiceUrl + gatewayPortId;
            }
        }
        PortWebBulkJson portResponse = restTemplate.getForObject(portManagerServiceUrl, PortWebBulkJson.class);
        if (portResponse == null) {
            throw new PortWebBulkJsonOrPortEntitiesListIsNull();
        }

        List<PortEntity> portEntitiesResponse = portResponse.getPortEntities();
        if (portEntitiesResponse == null) {
            throw new PortWebBulkJsonOrPortEntitiesListIsNull();
        }
        for (PortEntity portEntity : portEntitiesResponse) {
            List<PortEntity.FixedIp> fixedIps = portEntity.getFixedIps();
            if (fixedIps != null) {
                for (PortEntity.FixedIp fixedIp : fixedIps) {
                    String subnetId = fixedIp.getSubnetId();
                    subnetIds.add(subnetId);
                }
            }
        }


        return subnetIds;
    }

    @Override
    public void updatePort(String projectid, String portId, PortEntity portEntity) {
        String portManagerServiceUrl = portUrl + "/project/" + projectid + "/ports/" + portId;
        HttpEntity<PortWebJson> request = new HttpEntity<>(new PortWebJson(portEntity));
        restTemplate.put(portManagerServiceUrl, request, PortWebJson.class);
    }

    @Override
    public void updateL3Neighbors(String projectid, String vpcId, String subnetId, String operationType, List<String> gatewayPorts, InternalRouterInfo internalRouterInfo) {
        String portManagerServiceUrl = portUrl + "/project/" + projectid + "/update-l3-neighbors";
        RouterUpdateInfo routerUpdateInfo = new RouterUpdateInfo(vpcId, subnetId, operationType, gatewayPorts, internalRouterInfo);

        HttpEntity<RouterUpdateInfo> request = new HttpEntity<>(routerUpdateInfo);
        restTemplate.put(portManagerServiceUrl, request, RouterUpdateInfo.class);
    }
}
