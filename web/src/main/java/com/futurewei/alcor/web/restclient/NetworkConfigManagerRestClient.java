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

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
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
import java.util.logging.Level;

/**
 * API for DPM and NMM to push NCM info into NCM.
 */
@Configuration
public class NetworkConfigManagerRestClient extends AbstractRestClient {
    @Value("${microservices.ncm.service.url:#{\"\"}}")
    private String ncmManagerUrl;

    Logger LOG = LoggerFactory.getLogger();

    @DurationStatistics
    public void createNodeInfo(NodeInfoJson message) throws Exception {
        String ncmUrl = message.getNodeInfo().getNcmUri();
        if (ncmUrl == null)
            throw new Exception("NetworkConfigManagerClient: Required ncm_uri is NULL");
        HttpEntity<NodeInfoJson> request = new HttpEntity<>(message);
        restTemplate.postForObject(ncmManagerUrl, request, Object.class);
    }

    @DurationStatistics
    public void updateNodeInfo(NodeInfoJson message) throws Exception {
        String ncmUrl = message.getNodeInfo().getNcmUri();
        if (ncmUrl == null)
            throw new Exception("NetworkConfigManagerClient: Required ncm_uri is NULL");
        HttpEntity<NodeInfoJson> request = new HttpEntity<>(message);
        restTemplate.postForObject(ncmManagerUrl, request, Object.class);
    }

    @DurationStatistics
    public void deleteNodeInfo(String nodeId) throws Exception {
        /*
         * PROBLEM: To dispatch node deletion to the correct NCM, we need it's
         * URI but we can't get it here.
         * FIX: Change the API to pass in NodeInfo.
         * String ncmUrl = message.getNodeInfo().getNcmUri();
         *
            if (ncmUrl == null)
                throw new Exception("NetworkConfigManagerClient: Required ncm_uri is NULL");
         */
        HttpEntity<String> request = new HttpEntity<>(nodeId);
        restTemplate.postForObject(ncmManagerUrl, request, Object.class);
    }

    @DurationStatistics
    public void bulkCreatNodeInfo(BulkNodeInfoJson bulkNodeInfoJson) throws Exception {
        HttpEntity<BulkNodeInfoJson> request = new HttpEntity<>(bulkNodeInfoJson);
        restTemplate.postForObject(ncmManagerUrl, request, Object.class);
    }
}