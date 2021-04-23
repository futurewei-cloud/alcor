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
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * API for DPM and NMM to push NCM info into NCM.
 */
@Configuration
public class NetworkConfigManagerRestClient extends AbstractRestClient {
    Logger LOG = LoggerFactory.getLogger();

    @DurationStatistics
    public void createNodeInfo(NodeInfoJson message) throws Exception {
        String ncmUri = message.getNodeInfo().getNcmUri();
        if (ncmUri == null) {
            LOG.log(Level.INFO, "node with id " + message.getNodeInfo().getId() + " is getting created without an NCM");
            return;
        }
        HttpEntity<NodeInfoJson> request = new HttpEntity<>(message);
        restTemplate.postForObject(ncmUri, request, Object.class);
    }

    @DurationStatistics
    public void updateNodeInfo(NodeInfoJson message) throws Exception {
        String ncmUri = message.getNodeInfo().getNcmUri();
        if (ncmUri == null) {
            LOG.log(Level.INFO, "creating node with id " + message.getNodeInfo().getId() + " without an NCM");
            return;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<NodeInfoJson> request = new HttpEntity<>(message, headers);
        restTemplate.exchange(ncmUri, HttpMethod.PUT, request, NodeInfoJson.class).getBody();
    }

    @DurationStatistics
    public void deleteNodeInfo(String nodeId, String ncmUri) throws Exception {
        if (ncmUri == null) {
            LOG.log(Level.INFO, "deleting node with id " + nodeId + " without an NCM");
            return;
        }
        String delUrl = ncmUri + "/" + nodeId;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> request = new HttpEntity<>(headers);
        restTemplate.exchange(delUrl, HttpMethod.DELETE, request, String.class).getBody();
    }


    @DurationStatistics
    public void bulkCreatNodeInfo(BulkNodeInfoJson bulkNodeInfoJson) throws Exception {
        // This is assuming/requiring that bulk operations go to/must go to
        // single NCM.
        // NOTE: ncmUri already has been decorated with /bulk, so don't add
        // here again.
        String ncmUri = bulkNodeInfoJson.getNodeInfos().get(0).getNcmUri();
        if (ncmUri == null) {
            LOG.log(Level.INFO, "uploading nodes without an NCM");
            return;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<BulkNodeInfoJson> request = new HttpEntity<>(bulkNodeInfoJson, headers);
        restTemplate.postForObject(ncmUri, request, Object.class);
    }
}