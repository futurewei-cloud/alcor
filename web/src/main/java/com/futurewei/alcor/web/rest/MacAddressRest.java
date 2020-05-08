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
package com.futurewei.alcor.web.rest;

import com.futurewei.alcor.web.entity.MacState;
import com.futurewei.alcor.web.entity.MacStateJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class MacAddressRest extends AbstractRest {
    @Value("${microservices.mac.service.url:#{\"\"}}")
    private String macManagerUrl;

    @Bean
    public MacAddressRest macAddressRestInstance() {
        return new MacAddressRest();
    }

    private void verifyAllocatedMacAddress(MacStateJson result) throws Exception {
        if (result == null || result.getMacState() == null ||
                result.getMacState().getMacAddress() == null ||
                result.getMacState().getMacAddress().isEmpty()) {
            throw new Exception("Verify allocated mac address failed");
        }
    }

    public void releaseMacAddress(String macAddress) throws Exception {
        String url = macManagerUrl + "/" + macAddress;

        restTemplate.delete(url);
    }

    public MacStateJson allocateMacAddress(String projectId, String vpcId, String portId) throws Exception {
        MacState macState = new MacState();
        macState.setProjectId(projectId);
        macState.setVpcId(vpcId);
        macState.setPortId(portId);

        MacStateJson macStateJson = new MacStateJson();
        macStateJson.setMacState(macState);
        HttpEntity<MacStateJson> request = new HttpEntity<>(macStateJson);

        MacStateJson result = restTemplate.postForObject(macManagerUrl, request, MacStateJson.class);

        verifyAllocatedMacAddress(result);

        return result;
    }
}
