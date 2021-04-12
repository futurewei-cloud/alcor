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
package com.futurewei.alcor.portmanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.exception.UpdatePortIpException;
import com.futurewei.alcor.portmanager.processor.PortContext;
import com.futurewei.alcor.web.entity.ip.IpAddrRequest;
import com.futurewei.alcor.web.entity.ip.IpAddrUpdateRequest;
import com.futurewei.alcor.web.restclient.IpManagerRestClient;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UpdatePortIpAddressRequest extends AbstractRequest{

    private IpManagerRestClient ipManagerRestClient;
    private IpAddrUpdateRequest ipAddrUpdateRequest;
    private List<IpAddrUpdateRequest> result;

    public UpdatePortIpAddressRequest(PortContext context, IpAddrUpdateRequest ipAddrUpdateRequest) {
        super(context);
        this.ipAddrUpdateRequest = ipAddrUpdateRequest;
        this.result = new ArrayList<>();
        this.ipManagerRestClient = SpringContextUtil.getBean(IpManagerRestClient.class);
    }

    public List<IpAddrUpdateRequest> getResult(){
        return result;
    }

    @Override
    public void send() throws Exception {
        IpAddrUpdateRequest response = ipManagerRestClient.updateIpAddress(this.ipAddrUpdateRequest);
        if(response == null){
            throw new UpdatePortIpException();
        }
        result.add(response);
    }

    @Override
    public void rollback() throws Exception {
        log.info("UpdatePortIpAddressRequest rollback,oldIpAddrRequests is {}, newIpAddrRequests is {}",
                context.getFixedIpsresult().get(0).getOldIpAddrRequests(),context.getFixedIpsresult().get(0).getNewIpAddrRequests());
        List<IpAddrRequest> oldIpAddrRequests = context.getFixedIpsresult().get(0).getOldIpAddrRequests();
        List<IpAddrRequest> newIpAddrRequests = context.getFixedIpsresult().get(0).getNewIpAddrRequests();

        if(newIpAddrRequests.size() > 0){
            if(newIpAddrRequests.size() == 1){
                ipManagerRestClient.releaseIpAddress(newIpAddrRequests.get(0).getRangeId(),newIpAddrRequests.get(0).getIp());
            }else {
                ipManagerRestClient.releaseIpAddressBulk(newIpAddrRequests);
            }
        }

        if(oldIpAddrRequests.size() > 0){
            if(newIpAddrRequests.size() == 1){
                ipManagerRestClient.allocateIpAddress(null,null,
                        oldIpAddrRequests.get(0).getRangeId(),oldIpAddrRequests.get(0).getIp());
            }else {
                ipManagerRestClient.allocateIpAddressBulk(oldIpAddrRequests);
            }
        }
    }
}
