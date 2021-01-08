package com.futurewei.alcor.web.restclient;

import com.futurewei.alcor.web.entity.gateway.GatewayIpJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayManagerRestClinet extends AbstractRestClient{

    @Value("${microservices.zeta.service.url:\"\"}")
    private String zetaManagerUrl;

    @Value("${microservices.dpm.service.url:\"\"}")
    private String dpmManagerUrl;

    public GatewayIpJson createVPCInZetaGateway(Object args) {
        return null;
    }

    public String createDPMCacheGateway(Object args) {
        return null;
    }

    public String updateDPMCacheGateway(Object args) {
        return null;
    }

    public void deleteVPCInGateway(Object args) {

    }
}
