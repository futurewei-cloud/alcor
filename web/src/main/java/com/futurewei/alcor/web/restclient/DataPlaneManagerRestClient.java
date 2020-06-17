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
package com.futurewei.alcor.web.restclient;

import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;

public class DataPlaneManagerRestClient extends AbstractRestClient {
    @Value("${microservices.dataplane.service.url:#{\"\"}}")
    private String dataPlaneManagerUrl;

    public void createNetworkConfig(NetworkConfiguration message) throws Exception {
        HttpEntity<NetworkConfiguration> request = new HttpEntity<>(message);
        restTemplate.postForObject(dataPlaneManagerUrl, request, String[].class);
    }

    public void deleteNetworkConfig(NetworkConfiguration message) throws Exception {
        HttpEntity<NetworkConfiguration> request = new HttpEntity<>(message);
        restTemplate.put(dataPlaneManagerUrl, request);
    }

    public void updateNetworkConfig(NetworkConfiguration message) throws Exception {
        HttpEntity<NetworkConfiguration> request = new HttpEntity<>(message);
        restTemplate.put(dataPlaneManagerUrl, request, String[].class);
    }
}
