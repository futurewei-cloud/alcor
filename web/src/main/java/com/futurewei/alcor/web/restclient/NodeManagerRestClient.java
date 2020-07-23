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
import com.futurewei.alcor.web.entity.NodeInfo;
import com.futurewei.alcor.web.entity.NodeInfoJson;
import com.futurewei.alcor.web.entity.node.NodesWebJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import java.util.List;

@Configuration
public class NodeManagerRestClient extends AbstractRestClient {
    @Value("${microservices.node.service.url:#{\"\"}}")
    private String nodeManagerUrl;

    @DurationStatistics
    public NodeInfoJson getNodeInfo(String nodeId) throws Exception {
        String url = nodeManagerUrl + "/" + nodeId;
        return getForObject(url, NodeInfoJson.class);
    }

    @DurationStatistics
    public NodesWebJson getNodeInfoBulk(List<String> nodeIds) throws Exception {
        String queryParameter = buildQueryParameter("id", nodeIds);
        String url = nodeManagerUrl + "?" + queryParameter;
        return getForObject(url, NodesWebJson.class);
    }

    @DurationStatistics
    public List<NodeInfo> getNodeInfoByNodeName(String nodeName) throws Exception {
        String url = nodeManagerUrl + "?name=" + nodeName;
        ParameterizedTypeReference<List<NodeInfo>> responseType = new ParameterizedTypeReference<List<NodeInfo>>() {};
        ResponseEntity<List<NodeInfo>> resp = restTemplate.exchange(url, HttpMethod.GET, null,responseType);
        List<NodeInfo> list = resp.getBody();
        return list;
    }
}
