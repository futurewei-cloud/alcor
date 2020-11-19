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
package com.futurewei.alcor.route.service.Impl;

import com.futurewei.alcor.route.config.ConstantsConfig;
import com.futurewei.alcor.route.exception.DPMFailedHandleRequest;
import com.futurewei.alcor.route.service.RouterToDPMService;
import com.futurewei.alcor.web.entity.dataplane.InternalDPMResultList;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouterToDPMServiceImpl implements RouterToDPMService {

    @Value("${microservices.dpm.service.url}")
    private String dpmUrl;

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendInternalRouterInfoToDPM(InternalRouterInfo internalRouterInfo) throws Exception{
        NetworkConfiguration networkConfiguration = new NetworkConfiguration();
        List<InternalRouterInfo> internalRouterInfos = new ArrayList<>();
        internalRouterInfos.add(internalRouterInfo);
        networkConfiguration.setInternalRouterInfos(internalRouterInfos);

        String dpmServiceUrl = dpmUrl + "/network-configuration";
        HttpEntity<NetworkConfiguration> dpmRequest = new HttpEntity<>(networkConfiguration);
        InternalDPMResultList dpmResponse = restTemplate.postForObject(dpmServiceUrl, dpmRequest, InternalDPMResultList.class);
        if (dpmResponse == null) {
            throw new DPMFailedHandleRequest();
        }
        String resultMessage = dpmResponse.getResultMessage();
        if (resultMessage.equals(ConstantsConfig.DPMFailedHandleRequest)) {
            throw new DPMFailedHandleRequest();
        }

    }

}
