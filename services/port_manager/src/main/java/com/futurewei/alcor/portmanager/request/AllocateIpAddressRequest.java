/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
