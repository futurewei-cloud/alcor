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
package com.futurewei.alcor.privateipmanager.service.implement;

import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.privateipmanager.entity.IpAddrAlloc;
import com.futurewei.alcor.privateipmanager.entity.IpAddrRange;
import com.futurewei.alcor.web.entity.ip.*;
import com.futurewei.alcor.privateipmanager.exception.*;
import com.futurewei.alcor.privateipmanager.repo.IpAddrRangeRepo;
import com.futurewei.alcor.privateipmanager.service.IpAddrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class IpAddrServiceImpl implements IpAddrService {
    private static final Logger LOG = LoggerFactory.getLogger(IpAddrServiceImpl.class);

    @Autowired
    IpAddrRangeRepo ipAddrRangeRepo;

    @Override
    @DurationStatistics
    public IpAddrRequest allocateIpAddr(IpAddrRequest request) throws Exception {
        LOG.debug("Allocate ip address, request: {}", request);

        IpAddrAlloc ipAddrAlloc = ipAddrRangeRepo.allocateIpAddr(request);

        request.setIpVersion(ipAddrAlloc.getIpVersion());
        request.setSubnetId(ipAddrAlloc.getSubnetId());
        request.setRangeId(ipAddrAlloc.getRangeId());
        request.setIp(ipAddrAlloc.getIpAddr());
        request.setState(ipAddrAlloc.getState());

        LOG.info("Allocate ip address success, request: {}", request);

        return request;
    }

    @Override
    @DurationStatistics
    public IpAddrRequestBulk allocateIpAddrBulk(IpAddrRequestBulk requestBulk) throws Exception {
        LOG.debug("Allocate ip address bulk, requestBulk: {}", requestBulk);

        Map<String, List<IpAddrRequest>> rangeRequests = new HashMap<>();
        Map<String, List<IpAddrRequest>> vpcIpv4Requests = new HashMap<>();
        Map<String, List<IpAddrRequest>> vpcIpv6Requests = new HashMap<>();

        for (IpAddrRequest ipAddrRequest: requestBulk.getIpRequests()) {
            if (ipAddrRequest.getRangeId() != null) {
                if (!rangeRequests.containsKey(ipAddrRequest.getRangeId())) {
                    rangeRequests.put(ipAddrRequest.getRangeId(), new ArrayList<>());
                }

                rangeRequests.get(ipAddrRequest.getRangeId()).add(ipAddrRequest);
            } else if (ipAddrRequest.getVpcId() != null) {
                if (IpVersion.IPV4.getVersion() == ipAddrRequest.getIpVersion()) {
                    if (!vpcIpv4Requests.containsKey(ipAddrRequest.getVpcId())) {
                        vpcIpv4Requests.put(ipAddrRequest.getVpcId(), new ArrayList<>());
                    }

                    vpcIpv4Requests.get(ipAddrRequest.getVpcId()).add(ipAddrRequest);
                } else {
                    if (!vpcIpv6Requests.containsKey(ipAddrRequest.getVpcId())) {
                        vpcIpv6Requests.put(ipAddrRequest.getVpcId(), new ArrayList<>());
                    }

                    vpcIpv6Requests.get(ipAddrRequest.getVpcId()).add(ipAddrRequest);
                }
            }
        }

        List<IpAddrAlloc> ipAddrAllocList = ipAddrRangeRepo
                .allocateIpAddrBulk(rangeRequests, vpcIpv4Requests, vpcIpv6Requests);

        List<IpAddrRequest> result = new ArrayList<>();
        for (IpAddrAlloc ipAddrAlloc: ipAddrAllocList) {
            IpAddrRequest ipAddrRequest = new IpAddrRequest();
            ipAddrRequest.setIpVersion(ipAddrAlloc.getIpVersion());
            ipAddrRequest.setSubnetId(ipAddrAlloc.getSubnetId());
            ipAddrRequest.setRangeId(ipAddrAlloc.getRangeId());
            ipAddrRequest.setIp(ipAddrAlloc.getIpAddr());
            ipAddrRequest.setState(ipAddrAlloc.getState());
            result.add(ipAddrRequest);
        }

        requestBulk.setIpRequests(result);

        LOG.info("Allocate ip address bulk success, requestBulk: {}", requestBulk);

        return requestBulk;
    }

    @Override
    @DurationStatistics
    public IpAddrRequest modifyIpAddrState(IpAddrRequest request) throws Exception {
        LOG.debug("Modify ip address state, request: {}", request);

        ipAddrRangeRepo.modifyIpAddrState(request.getRangeId(), request.getIp(), request.getState());

        LOG.info("Modify ip address state success, request: {}", request);

        return request;
    }

    @Override
    @DurationStatistics
    public IpAddrRequestBulk modifyIpAddrStateBulk(IpAddrRequestBulk requestBulk) throws Exception {
        LOG.debug("Modify ip address state bulk, requestBulk: {}", requestBulk);

        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        for (IpAddrRequest request: requestBulk.getIpRequests()) {
            ipAddrRequests.add(modifyIpAddrState(request));
        }

        requestBulk.setIpRequests(ipAddrRequests);

        LOG.info("Modify ip address state bulk success, requestBulk: {}", requestBulk);

        return requestBulk;
    }

    @Override
    @DurationStatistics
    public void releaseIpAddr(String rangeId, String ipAddr) throws Exception {
        LOG.debug("Release ip address, ipAddr: {}", ipAddr);

        ipAddrRangeRepo.releaseIpAddr(rangeId, ipAddr);

        LOG.info("Release ip address success, result: {}", ipAddr);
    }

    @Override
    @DurationStatistics
    public void releaseIpAddrBulk(IpAddrRequestBulk requestBulk) throws Exception {
        LOG.debug("Release ip address bulk, requestBulk: {}", requestBulk);

        Map<String, List<String>> rangeToIpAddrList = new HashMap<>();

        for (IpAddrRequest request: requestBulk.getIpRequests()) {
            List<String> ipAddrList = rangeToIpAddrList.get(request.getRangeId());
            if (ipAddrList == null) {
                ipAddrList = new ArrayList<>();
                rangeToIpAddrList.put(request.getRangeId(), ipAddrList);
            }

            ipAddrList.add(request.getIp());

            request.setState(IpAddrState.FREE.getState());
        }

        ipAddrRangeRepo.releaseIpAddrBulk(rangeToIpAddrList);

        LOG.info("Release ip address bulk done, requestBulk: {}", requestBulk);
    }

    @Override
    @DurationStatistics
    public IpAddrRequest getIpAddr(String rangeId, String ipAddr) throws Exception {
        LOG.debug("Get ip address, rangeId: {}, ipAddr: {}", rangeId, ipAddr);

        IpAddrAlloc ipAddrAlloc = ipAddrRangeRepo.getIpAddr(rangeId, ipAddr);

        IpAddrRequest result = new IpAddrRequest();
        result.setIpVersion(ipAddrAlloc.getIpVersion());
        result.setSubnetId(ipAddrAlloc.getSubnetId());
        result.setRangeId(ipAddrAlloc.getRangeId());
        result.setIp(ipAddrAlloc.getIpAddr());
        result.setState(ipAddrAlloc.getState());

        LOG.info("Get ip address success, result: {}", result);

        return result;
    }

    @Override
    @DurationStatistics
    public List<IpAddrRequest> getIpAddrBulk(String rangeId) throws Exception {
        LOG.debug("List ip address, rangeId: {}", rangeId);

        List<IpAddrRequest> result = new ArrayList<>();

        Collection<IpAddrAlloc>  ipAddrAllocCollection = ipAddrRangeRepo.getIpAddrBulk(rangeId);
        for (IpAddrAlloc ipAddrAlloc: ipAddrAllocCollection) {
            IpAddrRequest ipAddr = new IpAddrRequest();
            ipAddr.setIpVersion(ipAddrAlloc.getIpVersion());
            ipAddr.setRangeId(ipAddrAlloc.getRangeId());
            ipAddr.setIp(ipAddrAlloc.getIpAddr());
            ipAddr.setState(ipAddrAlloc.getState());

            result.add(ipAddr);
        }

        LOG.info("List ip address success, result: {}", result);

        return result;
    }

    @Override
    @DurationStatistics
    public IpAddrRangeRequest createIpAddrRange(IpAddrRangeRequest request) throws Exception {
        LOG.debug("Create ip address range, request: {}", request);

        if (request.getId() == null) {
            request.setId(UUID.randomUUID().toString());
        }

        ipAddrRangeRepo.createIpAddrRange(request);

        LOG.info("Create ip address range success, request: {}", request);

        return request;
    }

    @Override
    @DurationStatistics
    public void deleteIpAddrRange(String rangeId) throws Exception {
        LOG.debug("Delete ip address range, rangeId: {}", rangeId);

        ipAddrRangeRepo.deleteIpAddrRange(rangeId);

        LOG.info("Delete ip address range success, rangeId: {}", rangeId);
    }

    @Override
    @DurationStatistics
    public IpAddrRangeRequest getIpAddrRange(String rangeId) throws Exception {
        LOG.debug("Get ip address range, rangeId: {}", rangeId);

        IpAddrRange ipAddrRange = ipAddrRangeRepo.getIpAddrRange(rangeId);
        if (ipAddrRange == null) {
            throw new IpAddrRangeNotFoundException();
        }

        LOG.info("Get ip address range success, ipAddressRange: {}", ipAddrRange);

        IpAddrRangeRequest result = new IpAddrRangeRequest();
        result.setId(ipAddrRange.getId());
        result.setVpcId(ipAddrRange.getVpcId());
        result.setSubnetId(ipAddrRange.getSubnetId());
        result.setIpVersion(ipAddrRange.getIpVersion());
        result.setFirstIp(ipAddrRange.getFirstIp());
        result.setLastIp(ipAddrRange.getLastIp());
        result.setUsedIps(ipAddrRange.getUsedIps());
        result.setTotalIps(ipAddrRange.getTotalIps());

        return result;
    }

    @Override
    @DurationStatistics
    public List<IpAddrRangeRequest> listIpAddrRange() {
        LOG.debug("List ip address range");

        Map<String, IpAddrRange> ipAddrRangeMap = ipAddrRangeRepo.findAllItems();

        List<IpAddrRangeRequest> result = new ArrayList<>();
        ipAddrRangeMap.forEach((k,v) -> {
            IpAddrRangeRequest range = new IpAddrRangeRequest();
            range.setId(v.getId());
            range.setVpcId(v.getVpcId());
            range.setSubnetId(v.getSubnetId());
            range.setIpVersion(v.getIpVersion());
            range.setFirstIp(v.getFirstIp());
            range.setLastIp(v.getLastIp());
            range.setUsedIps(v.getUsedIps());
            range.setTotalIps(v.getTotalIps());
            result.add(range);
        });

        LOG.info("List ip address range success, result: {}", result);

        return result;
    }

    @Override
    @DurationStatistics
    public List<IpAddrRequest> updateIpAddr(IpAddrUpdateRequest request) throws Exception {

        Map<String, List<String>> rangeToIpAddrList = null;

        Map<String, List<IpAddrRequest>> rangeRequests = null;
        Map<String, List<IpAddrRequest>> vpcIpv4Requests = null;
        Map<String, List<IpAddrRequest>> vpcIpv6Requests = null;

        if (request.getOldIpAddrRequests().size()>0){
            if (request.getOldIpAddrRequests().size()>1){
                LOG.debug("Release ip address bulk, requestBulk: {}", request.getOldIpAddrRequests());
                rangeToIpAddrList = new HashMap<>();
                for (IpAddrRequest ipAddrRequest: request.getOldIpAddrRequests()) {
                    List<String> ipAddrList = rangeToIpAddrList.computeIfAbsent(ipAddrRequest.getRangeId(), k -> new ArrayList<>());
                    ipAddrList.add(ipAddrRequest.getIp());
                    ipAddrRequest.setState(IpAddrState.FREE.getState());
                }
            }else {
                LOG.debug("Release ip address, ipAddr: {}", request.getOldIpAddrRequests().get(0).getIp());
            }
        }

        if (request.getNewIpAddrRequests().size()>0){
            if (request.getNewIpAddrRequests().size()>1){
                LOG.debug("Allocate ip address bulk, requestBulk: {}", request.getNewIpAddrRequests());
                rangeRequests = new HashMap<>();
                vpcIpv4Requests = new HashMap<>();
                vpcIpv6Requests = new HashMap<>();

                for (IpAddrRequest ipAddrRequest: request.getNewIpAddrRequests()) {
                    if (ipAddrRequest.getRangeId() != null) {
                        if (!rangeRequests.containsKey(ipAddrRequest.getRangeId())) {
                            rangeRequests.put(ipAddrRequest.getRangeId(), new ArrayList<>());
                        }
                        rangeRequests.get(ipAddrRequest.getRangeId()).add(ipAddrRequest);
                    } else if (ipAddrRequest.getVpcId() != null) {
                        if (IpVersion.IPV4.getVersion() == ipAddrRequest.getIpVersion()) {
                            if (!vpcIpv4Requests.containsKey(ipAddrRequest.getVpcId())) {
                                vpcIpv4Requests.put(ipAddrRequest.getVpcId(), new ArrayList<>());
                            }
                            vpcIpv4Requests.get(ipAddrRequest.getVpcId()).add(ipAddrRequest);
                        } else {
                            if (!vpcIpv6Requests.containsKey(ipAddrRequest.getVpcId())) {
                                vpcIpv6Requests.put(ipAddrRequest.getVpcId(), new ArrayList<>());
                            }
                            vpcIpv6Requests.get(ipAddrRequest.getVpcId()).add(ipAddrRequest);
                        }
                    }
                }
            }else {
                LOG.debug("Allocate ip address, request: {}", request.getNewIpAddrRequests().get(0));
            }
        }

        List<IpAddrAlloc> ipAddrAllocList = ipAddrRangeRepo.updateIpAddr(request, rangeToIpAddrList, rangeRequests, vpcIpv4Requests, vpcIpv6Requests);

        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        if(ipAddrAllocList!=null){
            for (IpAddrAlloc ipAddrAlloc: ipAddrAllocList) {
                IpAddrRequest ipAddrRequest = new IpAddrRequest();
                ipAddrRequest.setIpVersion(ipAddrAlloc.getIpVersion());
                ipAddrRequest.setSubnetId(ipAddrAlloc.getSubnetId());
                ipAddrRequest.setRangeId(ipAddrAlloc.getRangeId());
                ipAddrRequest.setIp(ipAddrAlloc.getIpAddr());
                ipAddrRequest.setState(ipAddrAlloc.getState());
                ipAddrRequests.add(ipAddrRequest);
            }
            LOG.info("Update Ip Success, ipAddrRequests: {}",ipAddrRequests);
        }

        return ipAddrRequests;
    }
}
