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
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import com.futurewei.alcor.web.entity.node.NodesWebJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

@Configuration
public class NodeManagerRestClient extends AbstractRestClient {
    @Value("${microservices.node.service.url:#{\"\"}}")
    private String nodeManagerUrl;

    @DurationStatistics
    public NodeInfoJson getNodeInfo(String nodeId) throws Exception {
        String queryParameter = buildQueryParameter("node_name", Collections.singletonList(nodeId));
        String url = nodeManagerUrl + "?" + queryParameter;
        NodesWebJson nodesWebJson = getForObject(url, NodesWebJson.class);
        if (nodesWebJson == null ||
                nodesWebJson.getNodeInfos() == null ||
                nodesWebJson.getNodeInfos().size() != 1) {
            return new NodeInfoJson();
        }

        return new NodeInfoJson(nodesWebJson.getNodeInfos().get(0));
    }

    @DurationStatistics
    public NodesWebJson getNodeInfoBulk(List<String> nodeIds) throws Exception {
        String queryParameter = buildQueryParameter("node_name", nodeIds);
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

    @DurationStatistics
    public List<NodeInfo> getNodeInfoByNodeIp(String nodeIp) throws Exception {
        String url = nodeManagerUrl + "?local_ip=" + nodeIp;
        return getForObject(url, NodesWebJson.class).getNodeInfos();
    }
}
