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
        log.info("UpdatePortIpAddressRequest rollback,ipAddrUpdateRequest is {}",ipAddrUpdateRequest);
        List<IpAddrRequest> newIpAddrRequests = ipAddrUpdateRequest.getNewIpAddrRequests();
        List<IpAddrRequest> oldIpAddrRequests = ipAddrUpdateRequest.getOldIpAddrRequests();

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
