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

import com.futurewei.alcor.web.entity.IpAddrRequest;
import com.futurewei.alcor.web.entity.IpAddrRequestBulk;
import com.futurewei.alcor.web.entity.IpAddrState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import java.util.List;

@Configuration
public class IpAddressRest extends AbstractRest {
    @Value("${microservices.ip.service.url:#{\"\"}}")
    private String ipManagerUrl;

    private void verifyAllocatedIpAddr(IpAddrRequest result) throws Exception {
        if (result == null || result.getIp() == null ||
                !IpAddrState.ACTIVATED.getState().equals(result.getState())) {
            throw new Exception("Verify allocated ip address failed");
        }
    }

    public void verifyIpAddress(String rangeId, String ip) throws Exception {
        String url = ipManagerUrl + "/" + rangeId + "/" + ip;

        IpAddrRequest ipAddrRequest = restTemplate.getForObject(url, IpAddrRequest.class);
        if (ipAddrRequest == null) {
            throw new Exception("Verify ip address failed");
        }
    }

    public IpAddrRequest allocateIpAddress(String rangeId, String ipAddr) throws Exception {
        IpAddrRequest ipAddrRequest = new IpAddrRequest();
        ipAddrRequest.setRangeId(rangeId);
        ipAddrRequest.setIp(ipAddr);

        HttpEntity<IpAddrRequest> request = new HttpEntity<>(ipAddrRequest);
        IpAddrRequest result = restTemplate.postForObject(ipManagerUrl, request, IpAddrRequest.class);

        verifyAllocatedIpAddr(result);

        return result;
    }

    public IpAddrRequestBulk allocateIpAddressBulk(List<IpAddrRequest> ipAddrRequests) throws Exception {
        IpAddrRequestBulk ipAddrRequestBulk = new IpAddrRequestBulk();
        ipAddrRequestBulk.setIpRequests(ipAddrRequests);

        String url = ipManagerUrl + "/bulk";
        HttpEntity<IpAddrRequestBulk> request = new HttpEntity<>(ipAddrRequestBulk);
        IpAddrRequestBulk result = restTemplate.postForObject(url , request, IpAddrRequestBulk.class);

        for (IpAddrRequest ipAddrRequest: result.getIpRequests()) {
            verifyAllocatedIpAddr(ipAddrRequest);
        }

        return result;
    }


    public void releaseIpAddress(String rangeId,String ip) throws Exception {
        String url = ipManagerUrl + "/" + rangeId + "/" + ip;

        restTemplate.delete(url);
    }

    public void releaseIpAddressBulk(List<IpAddrRequest> ipAddrRequests) throws Exception {
        IpAddrRequestBulk ipAddrRequestBulk = new IpAddrRequestBulk();
        ipAddrRequestBulk.setIpRequests(ipAddrRequests);

        String url = ipManagerUrl + "/bulk";
        HttpEntity<IpAddrRequestBulk> request = new HttpEntity<>(ipAddrRequestBulk);
        restTemplate.exchange(url, HttpMethod.DELETE, request, IpAddrRequestBulk.class);
    }
}
