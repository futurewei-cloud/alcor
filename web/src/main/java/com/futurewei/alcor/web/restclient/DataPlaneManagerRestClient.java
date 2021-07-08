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
import com.futurewei.alcor.web.entity.dataplane.InternalDPMResultList;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.gateway.GatewayInfo;
import com.futurewei.alcor.web.entity.gateway.ZetaPortsWebJson;
import com.futurewei.alcor.web.entity.node.BulkNodeInfoJson;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.Arrays;

@Configuration
@Import(RestTemplateConfig.class)
public class DataPlaneManagerRestClient extends AbstractRestClient {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${microservices.dataplane.service.url:#{\"\"}}")
    private String dataPlaneManagerUrl;

    @Value("${microservices.dataplane.nodeservice.url:#{\"\"}}")
    private String dataPlaneNodeManagerUrl;

    @Value("${microservices.zeta.management.url:#{\"\"}}")
    private String zetaManagerUrl;

    public DataPlaneManagerRestClient(RestTemplateBuilder restTemplateBuilder) {
        super(restTemplateBuilder);
    }

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
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<NodeInfoJson> request = new HttpEntity<>(message, headers);
        restTemplate.exchange(dataPlaneNodeManagerUrl, HttpMethod.PUT, request, NodeInfoJson.class).getBody();
    }

    @DurationStatistics
    public void deleteNodeInfo(NodeInfoJson message) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<NodeInfoJson> request = new HttpEntity<>(message, headers);
        restTemplate.exchange(dataPlaneNodeManagerUrl, HttpMethod.DELETE, request, NodeInfoJson.class).getBody();
    }

    @DurationStatistics
    public void bulkCreatNodeInfo(BulkNodeInfoJson bulkNodeInfoJson) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<BulkNodeInfoJson> request = new HttpEntity<>(bulkNodeInfoJson, headers);
        String bulkUri = dataPlaneNodeManagerUrl + "/bulk";
        restTemplate.postForObject(bulkUri, request, Object.class);
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