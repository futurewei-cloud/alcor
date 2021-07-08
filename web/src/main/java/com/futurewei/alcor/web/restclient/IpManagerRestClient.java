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

import com.futurewei.alcor.common.http.RestTemplateConfig;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.ip.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.util.List;

@Configuration
@Import(RestTemplateConfig.class)
public class IpManagerRestClient extends AbstractRestClient {
    @Value("${microservices.ip.service.url:#{\"\"}}")
    private String ipManagerUrl;

    public IpManagerRestClient(RestTemplateBuilder restTemplateBuilder) {
        super(restTemplateBuilder);
    }

    private void verifyAllocatedIpAddr(IpAddrRequest result) throws Exception {
        if (result == null || result.getIp() == null ||
                !IpAddrState.ACTIVATED.getState().equals(result.getState())) {
            throw new Exception("Verify allocated ip address failed");
        }
    }

    @DurationStatistics
    public void getIpAddress(String rangeId, String ip) throws Exception {
        String url = ipManagerUrl + "/" + rangeId + "/" + ip;

        IpAddrRequest ipAddrRequest = restTemplate.getForObject(url, IpAddrRequest.class);
        if (ipAddrRequest == null) {
            throw new Exception("Verify ip address failed");
        }
    }

    @DurationStatistics
    public IpAddrRequest allocateIpAddress(IpVersion ipVersion, String vpcId, String rangeId, String ipAddr) throws Exception {
        IpAddrRequest ipAddrRequest = new IpAddrRequest();
        if (ipVersion != null) {
            ipAddrRequest.setIpVersion(ipVersion.getVersion());
        }

        if (vpcId != null) {
            ipAddrRequest.setVpcId(vpcId);
        }

        if (rangeId != null) {
            ipAddrRequest.setRangeId(rangeId);
        }

        if (ipAddr != null) {
            ipAddrRequest.setIp(ipAddr);
        }

        HttpEntity<IpAddrRequest> request = new HttpEntity<>(ipAddrRequest);
        IpAddrRequest result = restTemplate.postForObject(ipManagerUrl, request, IpAddrRequest.class);

        verifyAllocatedIpAddr(result);

        return result;
    }

    @DurationStatistics
    public IpAddrRequest allocateIpAddress(IpAddrRequest ipAddrRequest) throws Exception {
        HttpEntity<IpAddrRequest> request = new HttpEntity<>(ipAddrRequest);
        IpAddrRequest result = restTemplate.postForObject(ipManagerUrl, request, IpAddrRequest.class);

        verifyAllocatedIpAddr(result);

        return result;
    }

    @DurationStatistics
    public IpAddrRequestBulk allocateIpAddressBulk(List<IpAddrRequest> ipAddrRequests) throws Exception {
        IpAddrRequestBulk ipAddrRequestBulk = new IpAddrRequestBulk();
        ipAddrRequestBulk.setIpRequests(ipAddrRequests);

        String url = ipManagerUrl + "/bulk";
        HttpEntity<IpAddrRequestBulk> request = new HttpEntity<>(ipAddrRequestBulk);
        IpAddrRequestBulk result = restTemplate.postForObject(url , request, IpAddrRequestBulk.class);

        for (IpAddrRequest ipAddrRequest : result.getIpRequests()) {
            verifyAllocatedIpAddr(ipAddrRequest);
        }

        return result;
    }

    @DurationStatistics
    public void releaseIpAddress(String rangeId, String ip) throws Exception {
        String url = ipManagerUrl + "/" + rangeId + "/" + ip;

        restTemplate.delete(url);
    }

    @DurationStatistics
    public void releaseIpAddressBulk(List<IpAddrRequest> ipAddrRequests) throws Exception {
        IpAddrRequestBulk ipAddrRequestBulk = new IpAddrRequestBulk();
        ipAddrRequestBulk.setIpRequests(ipAddrRequests);

        String url = ipManagerUrl + "/bulk";
        HttpEntity<IpAddrRequestBulk> request = new HttpEntity<>(ipAddrRequestBulk);
        restTemplate.exchange(url, HttpMethod.DELETE, request, IpAddrRequestBulk.class);
    }

    @DurationStatistics
    public IpAddrUpdateRequest updateIpAddress(IpAddrUpdateRequest ipAddrUpdateRequest) throws Exception {
        HttpEntity<IpAddrUpdateRequest> request = new HttpEntity<>(ipAddrUpdateRequest);
        IpAddrUpdateRequest result = restTemplate.postForObject(ipManagerUrl + "/update", request, IpAddrUpdateRequest.class);
        if(result != null && result.getNewIpAddrRequests().size() > 0){
            for (IpAddrRequest ipAddrRequest : result.getNewIpAddrRequests()) {
                verifyAllocatedIpAddr(ipAddrRequest);
            }
        }
        return result;
    }
}
