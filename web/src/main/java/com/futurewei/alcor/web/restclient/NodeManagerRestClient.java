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
package com.futurewei.alcor.web.restclient;

import com.futurewei.alcor.common.http.RestTemplateConfig;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import com.futurewei.alcor.web.entity.node.NodesWebJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

@Configuration
@Import(RestTemplateConfig.class)
public class NodeManagerRestClient extends AbstractRestClient {
    @Value("${microservices.node.service.url:#{\"\"}}")
    private String nodeManagerUrl;

    public NodeManagerRestClient(RestTemplateBuilder restTemplateBuilder) {
        super(restTemplateBuilder);
    }

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
