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

import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.entity.mac.MacStateBulkJson;
import com.futurewei.alcor.web.entity.mac.MacStateJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;

import java.util.List;

@Configuration
public class MacManagerRestClient extends AbstractRestClient {
    @Value("${microservices.mac.service.url:#{\"\"}}")
    private String macManagerUrl;

    private void verifyAllocatedMacAddress(MacStateJson result) throws Exception {
        if (result == null || result.getMacState() == null ||
                result.getMacState().getMacAddress() == null ||
                result.getMacState().getMacAddress().isEmpty()) {
            throw new Exception("Verify allocated mac address failed");
        }
    }

    private MacStateJson buildMacStateJson(String projectId, String vpcId, String portId, String macAddress) {
        MacState macState = new MacState(macAddress, projectId, vpcId, portId, null);
        return new MacStateJson(macState);
    }

    @DurationStatistics
    public void releaseMacAddress(String macAddress) throws Exception {
        String url = macManagerUrl + "/" + macAddress;

        restTemplate.delete(url);
    }

    @DurationStatistics
    public MacStateJson allocateMacAddress(String projectId, String vpcId, String portId, String macAddress) throws Exception {
        MacStateJson macStateJson = buildMacStateJson(projectId, vpcId, portId, macAddress);
        HttpEntity<MacStateJson> request = new HttpEntity<>(macStateJson);

        MacStateJson result = restTemplate.postForObject(macManagerUrl, request, MacStateJson.class);

        verifyAllocatedMacAddress(result);

        return result;
    }

    @DurationStatistics
    public MacStateJson allocateMacAddress(MacState macStates) throws Exception {
        MacStateJson macStateJson = new MacStateJson(macStates);
        HttpEntity<MacStateJson> request = new HttpEntity<>(macStateJson);

        MacStateJson result = restTemplate.postForObject(macManagerUrl, request, MacStateJson.class);

        verifyAllocatedMacAddress(result);

        return result;
    }

    @DurationStatistics
    public MacStateJson updateMacAddress(String projectId, String vpcId, String portId, String macAddress) throws Exception {
        MacStateJson macStateJson = buildMacStateJson(projectId, vpcId, portId, macAddress);
        HttpEntity<MacStateJson> request = new HttpEntity<>(macStateJson);

        restTemplate.put(macManagerUrl, request);

        return macStateJson;
    }

    @DurationStatistics
    public MacStateJson updateMacAddress(String macAddress, MacState macStates) throws Exception {
        MacStateJson macStateJson = new MacStateJson(macStates);
        HttpEntity<MacStateJson> request = new HttpEntity<>(macStateJson);
        String url = macManagerUrl + "/" + macAddress;
        restTemplate.put(url, request);

        return macStateJson;
    }

    @DurationStatistics
    public MacStateBulkJson allocateMacAddressBulk(List<MacState> macStates) throws Exception {
        MacStateBulkJson macStateBulkJson = new MacStateBulkJson(macStates);
        HttpEntity<MacStateBulkJson> request = new HttpEntity<>(macStateBulkJson);

        MacStateBulkJson result = restTemplate.postForObject(
                macManagerUrl + "/bulk", request, MacStateBulkJson.class);
        if (result == null || result.getMacStates() == null) {
            throw new Exception("Allocate mac addresses failed");
        }

        return result;
    }
}
