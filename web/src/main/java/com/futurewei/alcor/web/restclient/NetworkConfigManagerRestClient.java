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
import com.futurewei.alcor.web.entity.node.BulkNodeInfoJson;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import com.futurewei.alcor.web.entity.node.NodesWebJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

/**
 * API for DPM and NMM to push NCM info into NCM.
 */
@Configuration
public class NetworkConfigManagerRestClient extends AbstractRestClient {
    @Value("${microservices.ncm.service.url:#{\"\"}}")
    private String ncmManagerUrl;


    @DurationStatistics
    public void createNodeInfo(NodeInfoJson message) throws Exception {
        HttpEntity<NodeInfoJson> request = new HttpEntity<>(message);
        restTemplate.postForObject(ncmManagerUrl, request, Object.class);
    }

    @DurationStatistics
    public void updateNodeInfo(NodeInfoJson message) throws Exception {
        HttpEntity<NodeInfoJson> request = new HttpEntity<>(message);
        restTemplate.postForObject(ncmManagerUrl, request, Object.class);
    }

    @DurationStatistics
    public void deleteNodeInfo(String nodeId) throws Exception {
        HttpEntity<String> request = new HttpEntity<>(nodeId);
        restTemplate.postForObject(ncmManagerUrl, request, Object.class);
    }

    @DurationStatistics
    public void bulkCreatNodeInfo(BulkNodeInfoJson bulkNodeInfoJson) throws Exception {
        HttpEntity<BulkNodeInfoJson> request = new HttpEntity<>(bulkNodeInfoJson);
        restTemplate.postForObject(ncmManagerUrl, request, Object.class);
    }
}
