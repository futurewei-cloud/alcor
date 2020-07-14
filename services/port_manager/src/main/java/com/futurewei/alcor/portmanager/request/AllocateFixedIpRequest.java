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
package com.futurewei.alcor.portmanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.exception.AllocateIpAddrException;
import com.futurewei.alcor.web.entity.ip.IpAddrRequest;
import com.futurewei.alcor.web.restclient.IpManagerRestClient;

import java.util.ArrayList;
import java.util.List;

public class AllocateFixedIpRequest implements UpstreamRequest {
    private IpManagerRestClient ipManagerRestClient;
    private List<IpAddrRequest> fixedIpAddresses;
    private List<IpAddrRequest> ipAddresses;

    public AllocateFixedIpRequest(List<IpAddrRequest> fixedIpAddresses) {
        this.fixedIpAddresses = fixedIpAddresses;
        this.ipAddresses = new ArrayList<>();
        this.ipManagerRestClient = SpringContextUtil.getBean(IpManagerRestClient.class);
    }


    @Override
    public void send() throws Exception {
        //TODO: Instead by allocateMacAddresses interface
        for (IpAddrRequest ipAddrRequest: fixedIpAddresses) {
            IpAddrRequest response = ipManagerRestClient.allocateIpAddress(
                    null,
                    null,
                    ipAddrRequest.getRangeId(),
                    ipAddrRequest.getIp());
            if (response == null) {
                throw new AllocateIpAddrException();
            }

            ipAddresses.add(response);
        }
    }

    @Override
    public void rollback() throws Exception {
        //TODO: Instead by releaseMacAddresses interface
        for (IpAddrRequest ipAddrRequest: ipAddresses) {
            ipManagerRestClient.releaseIpAddress(ipAddrRequest.getRangeId(), ipAddrRequest.getIp());
        }
    }
}
