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
package com.futurewei.alcor.subnet.service.implement;

import com.futurewei.alcor.subnet.exception.PortWebJsonOrPortEntityIsNull;
import com.futurewei.alcor.subnet.service.SubnetToPortManagerService;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.PortWebJson;
import com.futurewei.alcor.web.entity.subnet.GatewayPortDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SubnetToPortManagerServiceImpl implements SubnetToPortManagerService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${microservices.port.service.url}")
    private String portUrl;

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public GatewayPortDetail createGatewayPort(String projectId, PortEntity portEntity) throws Exception {
        String portManagerServiceUrl = portUrl + "project/" + projectId + "/ports";
        HttpEntity<PortWebJson> portRequest = new HttpEntity<>(new PortWebJson(portEntity));
        PortWebJson portResponse = restTemplate.postForObject(portManagerServiceUrl, portRequest, PortWebJson.class);
        if (portResponse == null) {
            throw new PortWebJsonOrPortEntityIsNull();
        }

        PortEntity portEntityResponse = portResponse.getPortEntity();
        if (portEntityResponse == null) {
            throw new PortWebJsonOrPortEntityIsNull();
        }

        GatewayPortDetail gatewayPortDetail = new GatewayPortDetail(portEntityResponse.getMacAddress(), portEntityResponse.getId());

        return gatewayPortDetail;
    }

    @Override
    public void updateGatewayPort(String projectId, String portId, PortEntity portEntity) throws Exception {
        String portManagerServiceUrl = portUrl + "project/" + projectId + "/ports/" + portId;
        HttpEntity<PortWebJson> portRequest = new HttpEntity<>(new PortWebJson(portEntity));
        restTemplate.put(portManagerServiceUrl, portRequest, PortWebJson.class);
    }

    @Override
    public PortEntity getGatewayPortByPortID(String projectId, String portId) throws Exception {
        String portManagerServiceUrl = portUrl + "project/" + projectId + "/ports/" + portId;
        PortWebJson portResponse = restTemplate.getForObject(portManagerServiceUrl, PortWebJson.class);
        if (portResponse == null) {
            throw new PortWebJsonOrPortEntityIsNull();
        }

        PortEntity portEntityResponse = portResponse.getPortEntity();
        if (portEntityResponse == null) {
            throw new PortWebJsonOrPortEntityIsNull();
        }

        return portEntityResponse;
    }

    @Override
    public void deleteGatewayPort(String projectId, String portId) throws Exception {
        String portManagerServiceUrl = portUrl + "project/" + projectId + "/ports/" + portId;
        restTemplate.delete(portManagerServiceUrl);
    }

}
