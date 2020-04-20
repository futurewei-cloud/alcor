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

package com.futurewei.alcor.apigateway.subnet;

import com.futurewei.alcor.apigateway.proxies.SubnetManagerServiceProxy;
import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.utils.CommonUtil;
import com.futurewei.alcor.web.entity.SubnetWebJson;
import com.futurewei.alcor.web.entity.SubnetsWebJson;
import com.futurewei.alcor.web.exception.SubnetNotFoundException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

public class SubnetWebHandlers {

    private SubnetManagerServiceProxy subnetManagerServiceProxy;

    public SubnetWebHandlers(SubnetManagerServiceProxy subnetManagerServiceProxy) {
        this.subnetManagerServiceProxy = subnetManagerServiceProxy;
    }

    public Mono<ServerResponse> getSubnet(ServerRequest request) {
        UUID projectId = UUID.fromString(request.pathVariable("projectId"));
        UUID subnetId = UUID.fromString(request.pathVariable("subnetId"));

        Mono<SubnetWebJson> subnetInfo = subnetManagerServiceProxy.findSubnetById(projectId, subnetId);

        return subnetInfo.flatMap(od -> ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(fromObject(od)))
                .onErrorResume(SubnetNotFoundException.class, e -> ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createSubnet(ServerRequest request) {

        final Mono<SubnetWebJson> subnetObj = request.bodyToMono(SubnetWebJson.class);
        final UUID projectId = UUID.fromString(request.pathVariable("projectId"));

        final UUID generatedSubnetId = UUID.randomUUID();
        Mono<SubnetWebJson> newSubnetObj = subnetObj.map(p ->
                p.getSubnet().getId().isEmpty() || !CommonUtil.isUUID(p.getSubnet().getId()) ?
                        new SubnetWebJson(p.getSubnet(), generatedSubnetId) : new SubnetWebJson(p.getSubnet()));

        Mono<SubnetWebJson> response = subnetManagerServiceProxy.createSubnet(projectId, newSubnetObj);

        return response.flatMap(od -> ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(fromObject(od)))
                .onErrorResume(SubnetNotFoundException.class, e -> ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> updateSubnet(ServerRequest request) {

        final Mono<SubnetWebJson> updatedSubnetObj = request.bodyToMono(SubnetWebJson.class);
        UUID projectId = UUID.fromString(request.pathVariable("projectId"));
        UUID subnetId = UUID.fromString(request.pathVariable("subnetId"));

        Mono<SubnetWebJson> response = subnetManagerServiceProxy.updateSubnetById(projectId, subnetId, updatedSubnetObj);

        return response.flatMap(od -> ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(fromObject(od)))
                .onErrorResume(SubnetNotFoundException.class, e -> ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteSubnet(ServerRequest request) {

        UUID projectId = UUID.fromString(request.pathVariable("projectId"));
        UUID subnetId = UUID.fromString(request.pathVariable("subnetId"));

        Mono<ResponseId> responseWithId = subnetManagerServiceProxy.deleteSubnetById(projectId, subnetId);

        return responseWithId.flatMap(od -> ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(fromObject(od)))
                .onErrorResume(SubnetNotFoundException.class, e -> ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getSubnetManagerHealthStatus(ServerRequest request) {

        Mono<String> healthStatus = subnetManagerServiceProxy.getHealthStatus();

        return healthStatus.flatMap(od -> ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(fromObject(od)))
                .onErrorResume(e -> ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getSubnets(ServerRequest request) {
        UUID projectId = UUID.fromString(request.pathVariable("projectId"));

        Mono<SubnetsWebJson> subnetsInfo = subnetManagerServiceProxy.findSubnets(projectId);

        return subnetsInfo.flatMap(od -> ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(fromObject(od)))
                .onErrorResume(SubnetNotFoundException.class, e -> ServerResponse.notFound().build());
    }

}
