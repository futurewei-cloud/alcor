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

package com.futurewei.alcor.apigateway.proxies;

import com.futurewei.alcor.apigateway.subnet.SubnetWebDestinations;
import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.entity.subnet.SubnetsWebJson;
import com.futurewei.alcor.web.exception.SubnetNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class SubnetManagerServiceProxy {

    private SubnetWebDestinations webDestinations;

    private WebClient webClient;

    public SubnetManagerServiceProxy(SubnetWebDestinations destinations, WebClient subnetManagerWebClient) {
        this.webDestinations = destinations;
        this.webClient = subnetManagerWebClient;
    }

    public Mono<SubnetWebJson> findSubnetById(UUID projectId, UUID subnetId) {
        Mono<ClientResponse> response = webClient
                .get()
                .uri(webDestinations.getSubnetManagerServiceUrl() + "/project/{projectId}/subnets/{subnetId}", projectId, subnetId)
                .exchange();
        return response.flatMap(resp -> {
            switch (resp.statusCode()) {
                case OK:
                    return resp.bodyToMono(SubnetWebJson.class);
                case NOT_FOUND:
                    return Mono.error(new SubnetNotFoundException());
                default:
                    return Mono.error(new RuntimeException("Unknown" + resp.statusCode()));
            }
        });
    }

    public Mono<SubnetWebJson> createSubnet(UUID projectId, Mono<SubnetWebJson> newSubnetJson) {
        Mono<ClientResponse> response = webClient
                .post()
                .uri(webDestinations.getSubnetManagerServiceUrl() + "/project/{projectId}/subnets", projectId)
                .body(newSubnetJson, SubnetWebJson.class)
                .exchange();
        return response.flatMap(resp -> {
            switch (resp.statusCode()) {
                case CREATED:
                    return resp.bodyToMono(SubnetWebJson.class);
                case NOT_FOUND:
                    return Mono.error(new SubnetNotFoundException());
                default:
                    return Mono.error(new RuntimeException("Unknown" + resp.statusCode()));
            }
        });
    }

    public Mono<SubnetWebJson> updateSubnetById(UUID projectId, UUID subnetId, Mono<SubnetWebJson> updatedSubnetJson) {
        Mono<ClientResponse> response = webClient
                .put()
                .uri(webDestinations.getSubnetManagerServiceUrl() + "/project/{projectId}/subnets/{subnetId}", projectId, subnetId)
                .body(updatedSubnetJson, SubnetWebJson.class)
                .exchange();
        return response.flatMap(resp -> {
            switch (resp.statusCode()) {
                case OK:
                    return resp.bodyToMono(SubnetWebJson.class);
                case NOT_FOUND:
                    return Mono.error(new SubnetNotFoundException());
                default:
                    return Mono.error(new RuntimeException("Unknown" + resp.statusCode()));
            }
        });
    }

    public Mono<ResponseId> deleteSubnetById(UUID projectId, UUID subnetId) {
        Mono<ClientResponse> response = webClient
                .delete()
                .uri(webDestinations.getSubnetManagerServiceUrl() + "/project/{projectId}/subnets/{subnetId}", projectId, subnetId)
                .exchange();
        return response.flatMap(resp -> {
            switch (resp.statusCode()) {
                case OK:
                    return resp.bodyToMono(ResponseId.class);
                case NOT_FOUND:
                    return Mono.error(new SubnetNotFoundException());
                default:
                    return Mono.error(new RuntimeException("Unknown" + resp.statusCode()));
            }
        });
    }

    public Mono<SubnetsWebJson> findSubnets(UUID projectId) {
        Mono<ClientResponse> response = webClient
                .get()
                .uri(webDestinations.getSubnetManagerServiceUrl() + "/project/{projectId}/subnets", projectId)
                .exchange();
        return response.flatMap(resp -> {
            switch (resp.statusCode()) {
                case OK:
                    return resp.bodyToMono(SubnetsWebJson.class);
                case NOT_FOUND:
                    return Mono.error(new SubnetNotFoundException());
                default:
                    return Mono.error(new RuntimeException("Unknown" + resp.statusCode()));
            }
        });
    }

    public Mono<String> getHealthStatus() {
        Mono<ClientResponse> healthStatusResponse = webClient
                .get()
                .uri(webDestinations.getSubnetManagerServiceUrl() + "/actuator/health")
                .exchange();
        return healthStatusResponse.flatMap(resp -> {
            switch (resp.statusCode()) {
                case OK:
                    return resp.bodyToMono(String.class);
                default:
                    return Mono.error(new RuntimeException("Unknown" + resp.statusCode()));
            }
        });
    }
}
