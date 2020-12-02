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

import com.futurewei.alcor.route.exception.PortWebBulkJsonOrPortEntitiesListIsNull;
import com.futurewei.alcor.route.exception.PortWebJsonOrPortEntityIsNull;
import com.futurewei.alcor.route.service.RouterToPMService;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.PortWebBulkJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouterToPMServiceImpl implements RouterToPMService {

    @Value("${microservices.port.service.url}")
    private String portUrl;

    private RestTemplate restTemplate = new RestTemplate();

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
}
