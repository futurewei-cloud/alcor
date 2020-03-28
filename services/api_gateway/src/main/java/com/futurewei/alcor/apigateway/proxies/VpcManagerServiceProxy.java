package com.futurewei.alcor.apigateway.proxies;

import com.futurewei.alcor.apigateway.vpc.VpcWebDestinations;
import com.futurewei.alcor.web.entity.VpcWebJson;
import com.futurewei.alcor.web.exception.VpcNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class VpcManagerServiceProxy {

    private VpcWebDestinations vpcWebDestinations;

    private WebClient webClient;

    public VpcManagerServiceProxy(VpcWebDestinations destinations, WebClient client) {
        this.vpcWebDestinations = destinations;
        this.webClient = client;
    }

    public Mono<VpcWebJson> findVpcById(String projectId, String vpcId) {
        Mono<ClientResponse> response = webClient
                .get()
                .uri(vpcWebDestinations.getVpcManagerServiceUrl() + "/project/projectId}/vpc/{vpcId}", projectId, vpcId)
                .exchange();
        return response.flatMap(resp -> {
            switch (resp.statusCode()) {
                case OK:
                    return resp.bodyToMono(VpcWebJson.class);
                case NOT_FOUND:
                    return Mono.error(new VpcNotFoundException());
                default:
                    return Mono.error(new RuntimeException("Unknown" + resp.statusCode()));
            }
        });
    }
}
