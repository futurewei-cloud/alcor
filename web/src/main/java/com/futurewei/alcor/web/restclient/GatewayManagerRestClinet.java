package com.futurewei.alcor.web.restclient;

import com.futurewei.alcor.web.entity.gateway.GatewayInfo;
import com.futurewei.alcor.web.entity.gateway.GatewayInfoJson;
import com.futurewei.alcor.web.entity.gateway.GatewayIpJson;
import com.futurewei.alcor.web.entity.gateway.VpcInfoSub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;

@Configuration
@EnableRetry
@Slf4j
public class GatewayManagerRestClinet extends AbstractRestClient {

    @Value("${microservices.zeta.service.url:\"\"}")
    private String zetaManagerUrl;

    @Value("${microservices.dpm.service.url:\"\"}")
    private String dpmManagerUrl;

    public GatewayIpJson createVPCInZetaGateway(Object args) throws Exception {
        String url = zetaManagerUrl + "/vpcs";
        VpcInfoSub vpcInfoSub = (VpcInfoSub) args;
        HttpEntity<VpcInfoSub> request = new HttpEntity<>(vpcInfoSub);
        return restTemplate.postForObject(url, request, GatewayIpJson.class);
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
}