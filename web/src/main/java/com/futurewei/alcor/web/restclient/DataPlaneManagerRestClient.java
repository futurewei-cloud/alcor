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
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;

@Configuration
public class DataPlaneManagerRestClient extends AbstractRestClient {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${microservices.dataplane.service.url:#{\"\"}}")
    private String dataPlaneManagerUrl;

    @Value("${microservices.dataplane.nodeservice.url:#{\"\"}}")
    private String dataPlaneNodeManagerUrl;

    @DurationStatistics
    public void createNetworkConfig(NetworkConfiguration message) throws Exception {
        HttpEntity<NetworkConfiguration> request = new HttpEntity<>(message);
        //logger.info(new ObjectMapper().writeValueAsString(request));
        restTemplate.postForObject(dataPlaneManagerUrl, request, Object.class);
    }

    @DurationStatistics
    public void deleteNetworkConfig(NetworkConfiguration message) throws Exception {
        HttpEntity<NetworkConfiguration> request = new HttpEntity<>(message);
        restTemplate.put(dataPlaneManagerUrl, request);
    }

    @DurationStatistics
    public void updateNetworkConfig(NetworkConfiguration message) throws Exception {
        HttpEntity<NetworkConfiguration> request = new HttpEntity<>(message);
        restTemplate.put(dataPlaneManagerUrl, request, String[].class);
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

}
