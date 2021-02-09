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

    @Value("${microservices.zeta.service.url:\"\"}")
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
        //return restTemplate.postForObject(url, request, ZetaGatewayIpJson.class);
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
