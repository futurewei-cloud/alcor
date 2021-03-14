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

import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.dataplane.InternalDPMResultList;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.gateway.GatewayInfo;
import com.futurewei.alcor.web.entity.gateway.ZetaPortsWebJson;
import com.futurewei.alcor.web.entity.node.BulkNodeInfoJson;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

@Configuration
public class DataPlaneManagerRestClient extends AbstractRestClient {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${microservices.dataplane.service.url:#{\"\"}}")
    private String dataPlaneManagerUrl;

    @Value("${microservices.dataplane.nodeservice.url:#{\"\"}}")
    private String dataPlaneNodeManagerUrl;

    @Value("${microservices.zeta.management.url:#{\"\"}}")
    private String zetaManagerUrl;

    @DurationStatistics
    public InternalDPMResultList createNetworkConfig(NetworkConfiguration message) throws Exception {
        HttpEntity<NetworkConfiguration> request = new HttpEntity<>(message);
        //logger.info(new ObjectMapper().writeValueAsString(request));
        return restTemplate.postForObject(dataPlaneManagerUrl, request, InternalDPMResultList.class);
    }

    @DurationStatistics
    public void deleteNetworkConfig(NetworkConfiguration message) throws Exception {
        HttpEntity<NetworkConfiguration> request = new HttpEntity<>(message);
        restTemplate.put(dataPlaneManagerUrl, request);
    }

    @DurationStatistics
    public InternalDPMResultList updateNetworkConfig(NetworkConfiguration message) throws Exception {
        HttpEntity<NetworkConfiguration> request = new HttpEntity<>(message);
        return restTemplate.exchange(dataPlaneManagerUrl, HttpMethod.PUT, request, InternalDPMResultList.class).getBody();
    }

    @DurationStatistics
    public void createNodeInfo(NodeInfoJson message) throws Exception {
        HttpEntity<NodeInfoJson> request = new HttpEntity<>(message);
        restTemplate.postForObject(dataPlaneNodeManagerUrl, request, Object.class);
    }

    @DurationStatistics
    public void updateNodeInfo(NodeInfoJson message) throws Exception {
        HttpEntity<NodeInfoJson> request = new HttpEntity<>(message);
        restTemplate.postForObject(dataPlaneNodeManagerUrl, request, Object.class);
    }

    @DurationStatistics
    public void deleteNodeInfo(String nodeId) throws Exception {
        HttpEntity<String> request = new HttpEntity<>(nodeId);
        restTemplate.postForObject(dataPlaneNodeManagerUrl, request, Object.class);
    }

    @DurationStatistics
    public void bulkCreatNodeInfo(BulkNodeInfoJson bulkNodeInfoJson) throws Exception {
        HttpEntity<BulkNodeInfoJson> request = new HttpEntity<>(bulkNodeInfoJson);
        restTemplate.postForObject(dataPlaneNodeManagerUrl, request, Object.class);
    }


    @DurationStatistics
    public void createGatewayInfo(GatewayInfo message) throws Exception {
        HttpEntity<GatewayInfo> request = new HttpEntity<>(message);
        restTemplate.postForObject(dataPlaneManagerUrl, request, String[].class);
    }

    @DurationStatistics
    public void updateGatewayInfo(GatewayInfo message) throws Exception {
        HttpEntity<GatewayInfo> request = new HttpEntity<>(message);
        restTemplate.put(dataPlaneManagerUrl, request, String[].class);
    }

    @DurationStatistics
    public ZetaPortsWebJson createPortInZetaGateway(Object args) throws Exception {
        String url = zetaManagerUrl + "/ports";
        ZetaPortsWebJson zetaPortEntities = (ZetaPortsWebJson) args;
        HttpEntity<ZetaPortsWebJson> request = new HttpEntity<>(zetaPortEntities);
        return restTemplate.postForObject(url, request, ZetaPortsWebJson.class);
    }
}
