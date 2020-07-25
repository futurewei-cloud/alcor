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
import com.futurewei.alcor.portmanager.processor.PortContext;
import com.futurewei.alcor.web.entity.ip.IpAddrRequest;
import com.futurewei.alcor.web.entity.ip.IpAddrRequestBulk;
import com.futurewei.alcor.web.entity.ip.IpVersion;
import com.futurewei.alcor.web.restclient.IpManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AllocateIpAddressRequest extends AbstractRequest {
    private static final Logger LOG = LoggerFactory.getLogger(AllocateIpAddressRequest.class);

    private IpManagerRestClient ipManagerRestClient;
    private List<IpAddrRequest> ipRequests;
    private List<IpAddrRequest> result;

    public AllocateIpAddressRequest(PortContext context, List<IpAddrRequest> ipRequests) {
        super(context);
        this.ipRequests = ipRequests;
        this.result = new ArrayList<>();
        this.ipManagerRestClient = SpringContextUtil.getBean(IpManagerRestClient.class);
    }

    public List<IpAddrRequest> getResult() {
        return result;
    }

    @Override
    public void send() throws Exception {
        if (ipRequests.size() == 1) {
            IpAddrRequest response = ipManagerRestClient.allocateIpAddress(ipRequests.get(0));
            if (response == null) {
                throw new AllocateIpAddrException();
            }

            result.add(response);
        } else {
            IpAddrRequestBulk response = ipManagerRestClient.allocateIpAddressBulk(ipRequests);
            if (response == null ||
                    response.getIpRequests() == null ||
                    response.getIpRequests().size() != ipRequests.size()) {
                throw new AllocateIpAddrException();
            }

            result.addAll(response.getIpRequests());
        }
    }

    @Override
    public void rollback() throws Exception {
        LOG.info("AllocateRandomIpRequest rollback, ipAddresses: {}", result);
        if (result.size() == 1) {
            ipManagerRestClient.releaseIpAddress(result.get(0).getRangeId(), result.get(0).getIp());
        } else {
            ipManagerRestClient.releaseIpAddressBulk(result);
        }
    }
}
