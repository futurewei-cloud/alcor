/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.web.restclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.web.entity.gateway.GatewayInfo;
import com.futurewei.alcor.web.entity.gateway.GatewayInfoJson;
import com.futurewei.alcor.web.entity.gateway.VpcInfoSub;
import com.futurewei.alcor.web.entity.gateway.ZetaGatewayIpJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;

@Configuration
@EnableRetry
@Slf4j
public class GatewayManagerRestClient extends AbstractRestClient {

    @Value("${microservices.zeta.management.url:\"\"}")
    private String zetaManagerUrl;

    @Value("${microservices.dpm.service.url:\"\"}")
    private String dpmManagerUrl;

    public ZetaGatewayIpJson createVPCInZetaGateway(Object args) throws Exception {
        String url = zetaManagerUrl + "/vpcs";
        VpcInfoSub vpcInfoSub = (VpcInfoSub) args;
        ObjectMapper Obj = new ObjectMapper();
        String jsonStr = Obj.writeValueAsString(vpcInfoSub);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonStr, headers);
        Object response = restTemplate.postForObject(url, request, Object.class);
        ZetaGatewayIpJson result = new ZetaGatewayIpJson();
        if (response != null) {
            ObjectMapper mapper = new ObjectMapper();
            result = mapper.convertValue(response, new TypeReference<ZetaGatewayIpJson>() {
            });
        }
        return result;
    }

    public void deleteVPCInZetaGateway(String vpcId) throws Exception {
        String url = zetaManagerUrl + "/vpcs/" + vpcId;
        restTemplate.delete(url);
    }

    @Retryable(maxAttempts = 4)
    public String createDPMCacheGateway(Object args1, Object args2) throws Exception {
        log.info("send request to create DPM GatewayInfo cache");
        String projectId = (String) args1;
        GatewayInfo gatewayInfo = (GatewayInfo) args2;
        String url = dpmManagerUrl + "/project/" + projectId + "/gatewayinfo";
        HttpEntity<GatewayInfoJson> request = new HttpEntity<>(new GatewayInfoJson(gatewayInfo));
        return restTemplate.postForObject(url, request, String.class);
    }

    public void updateDPMCacheGateway(String projectId, GatewayInfo gatewayInfo) throws Exception {
        String url = dpmManagerUrl + "/project/" + projectId + "/gatewayinfo/" + gatewayInfo.getResourceId();
        HttpEntity<GatewayInfoJson> request = new HttpEntity<>(new GatewayInfoJson(gatewayInfo));
        restTemplate.put(url, request);
    }

    public void deleteDPMCacheGateway(String projectId, String vpcId) {
        String url = dpmManagerUrl + "/project/" + projectId + "/gatewayinfo/" + vpcId;
        restTemplate.delete(url);
    }
}
