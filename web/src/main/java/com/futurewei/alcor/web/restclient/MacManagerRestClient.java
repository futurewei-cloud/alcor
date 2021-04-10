/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
