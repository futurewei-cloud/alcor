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

package com.futurewei.alcor.privateipmanager.service.implement;

import com.futurewei.alcor.privateipmanager.entity.*;
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

    public IpAddrRequest allocateIpAddr(IpAddrRequest request) throws Exception {
        LOG.debug("Allocate ip address, request: {}", request);

        String ipAddr = ipAddrRangeRepo.allocateIpAddr(request.getSubnetId());

        request.setIpAddr(ipAddr);
        request.setState(IpAddrState.ACTIVATED.getState());

        LOG.info("Allocate ip address success, request: {}", request);

        return request;
    }

    public IpAddrRequestBulk allocateIpAddrBulk(IpAddrRequestBulk requestBulk) throws Exception {
        LOG.debug("Allocate ip address bulk, requestBulk: {}", requestBulk);

        Map<String, Integer> subnetToNum = new HashMap<>();
        Map<String, Integer> subnetToIpVersion = new HashMap<>();
        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();

        for (IpAddrRequest request: requestBulk.getIpAddrRequests()) {
            Integer num = 1;

            if (subnetToNum.containsKey(request.getSubnetId())) {
                num = subnetToNum.get(request.getSubnetId()) + 1;
            }

            subnetToNum.put(request.getSubnetId(), num);
            subnetToIpVersion.put(request.getSubnetId(), request.getIpVersion());
        }

        Map<String, List<String>> result = ipAddrRangeRepo.allocateIpAddrBulk(subnetToNum);

        for (Map.Entry<String, List<String>> entry: result.entrySet()) {
            for (String ipAddr: entry.getValue()) {
                IpAddrRequest ipAddrRequest = new IpAddrRequest();
                ipAddrRequest.setIpVersion(subnetToIpVersion.get(entry.getKey()));
                ipAddrRequest.setSubnetId(entry.getKey());
                ipAddrRequest.setIpAddr(ipAddr);
                ipAddrRequest.setState(IpAddrState.ACTIVATED.getState());
                ipAddrRequests.add(ipAddrRequest);
            }
        }

        requestBulk.setIpAddrRequests(ipAddrRequests);

        LOG.info("Allocate ip address bulk success, requestBulk: {}", requestBulk);

        return requestBulk;
    }

    public IpAddrRequest modifyIpAddrState(IpAddrRequest request) throws Exception {
        LOG.debug("Modify ip address state, request: {}", request);

        ipAddrRangeRepo.modifyIpAddrState(request.getSubnetId(), request.getIpAddr(), request.getState());

        LOG.info("Modify ip address state success, request: {}", request);

        return request;
    }

    public IpAddrRequestBulk modifyIpAddrStateBulk(IpAddrRequestBulk requestBulk) throws Exception {
        LOG.debug("Modify ip address state bulk, requestBulk: {}", requestBulk);

        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        for (IpAddrRequest request: requestBulk.getIpAddrRequests()) {
            ipAddrRequests.add(modifyIpAddrState(request));
        }

        requestBulk.setIpAddrRequests(ipAddrRequests);

        LOG.info("Modify ip address state bulk success, requestBulk: {}", requestBulk);

        return requestBulk;
    }

    public IpAddrRequest releaseIpAddr(int ipVersion, String subnetId, String ipAddr) throws Exception {
        LOG.debug("Release ip address, ipAddr: {}", ipAddr);

        ipAddrRangeRepo.releaseIpAddr(subnetId, ipAddr);

        IpAddrRequest result = new IpAddrRequest();
        result.setIpVersion(ipVersion);
        result.setSubnetId(subnetId);
        result.setIpAddr(ipAddr);
        result.setState(IpAddrState.FREE.getState());

        LOG.info("Release ip address success, result: {}", result);

        return result;
    }

    public IpAddrRequestBulk releaseIpAddrBulk(IpAddrRequestBulk requestBulk) throws Exception {
        LOG.debug("Release ip address bulk, requestBulk: {}", requestBulk);

        Map<String, List<String>> subnetToIpAddrList = new HashMap<>();

        for (IpAddrRequest request: requestBulk.getIpAddrRequests()) {
            List<String> ipAddrList = subnetToIpAddrList.get(request.getSubnetId());
            if (ipAddrList == null) {
                ipAddrList = new ArrayList<>();
                subnetToIpAddrList.put(request.getSubnetId(), ipAddrList);
            }

            ipAddrList.add(request.getIpAddr());

            request.setState(IpAddrState.FREE.getState());
        }

        ipAddrRangeRepo.releaseIpAddrBulk(subnetToIpAddrList);

        LOG.info("Release ip address bulk done, requestBulk: {}", requestBulk);

        return requestBulk;
    }

    public IpAddrRequest getIpAddr(int ipVersion, String subnetId, String ipAddr) throws Exception {
        LOG.debug("Get ip address, subnetId: {}, ipAddr: {}", subnetId, ipAddr);

        IpAddrAlloc ipAddrAlloc = ipAddrRangeRepo.getIpAddr(subnetId, ipAddr);
        if (ipAddrAlloc.getIpVersion() != ipVersion) {
            throw new IpVersionInvalidException();
        }

        IpAddrRequest result = new IpAddrRequest();

        result.setIpVersion(ipAddrAlloc.getIpVersion());
        result.setSubnetId(ipAddrAlloc.getSubnetId());
        result.setIpAddr(ipAddrAlloc.getIpAddr());
        result.setState(ipAddrAlloc.getState());

        LOG.info("Get ip address success, result: {}", result);

        return result;
    }

    public List<IpAddrRequest> getIpAddrBulk(String subnetId) throws Exception {
        LOG.debug("List ip address, subnetId: {}", subnetId);

        List<IpAddrRequest> result = new ArrayList<>();

        Collection<IpAddrAlloc>  ipAddrAllocCollection = ipAddrRangeRepo.getIpAddrBulk(subnetId);
        for (IpAddrAlloc ipAddrAlloc: ipAddrAllocCollection) {
            IpAddrRequest ipAddr = new IpAddrRequest();
            ipAddr.setIpVersion(ipAddrAlloc.getIpVersion());
            ipAddr.setSubnetId(ipAddrAlloc.getSubnetId());
            ipAddr.setIpAddr(ipAddrAlloc.getIpAddr());
            ipAddr.setState(ipAddrAlloc.getState());

            result.add(ipAddr);
        }

        LOG.info("List ip address success, result: {}", result);

        return result;
    }

    public IpAddrRangeRequest createIpAddrRange(IpAddrRangeRequest request) throws Exception {
        LOG.debug("Create ip address range, request: {}", request);

        ipAddrRangeRepo.createIpAddrRange(request);

        LOG.info("Create ip address range success, request: {}", request);

        return request;
    }

    public IpAddrRangeRequest deleteIpAddrRange(String subnetId) throws Exception {
        LOG.debug("Delete ip address range, subnetId: {}", subnetId);

        IpAddrRange ipAddrRange = ipAddrRangeRepo.deleteIpAddrRange(subnetId);

        IpAddrRangeRequest request = new IpAddrRangeRequest();
        request.setSubnetId(ipAddrRange.getSubnetId());
        request.setFirstAddr(ipAddrRange.getFirstAddr());
        request.setLastAddr(ipAddrRange.getLastAddr());

        LOG.info("Delete ip address range success, request: {}", request);

        return request;
    }

    public IpAddrRangeRequest getIpAddrRange(String subnetId) throws Exception {
        LOG.debug("Get ip address range, subnetId: {}", subnetId);

        IpAddrRange ipAddrRange = ipAddrRangeRepo.getIpAddrRange(subnetId);
        if (ipAddrRange == null) {
            throw new IpAddrRangeNotFoundException();
        }

        LOG.info("Get ip address range success, ipAddressRange: {}", ipAddrRange);

        IpAddrRangeRequest result = new IpAddrRangeRequest();
        result.setSubnetId(ipAddrRange.getSubnetId());
        result.setFirstAddr(ipAddrRange.getFirstAddr());
        result.setLastAddr(ipAddrRange.getLastAddr());

        return result;
    }

    public List<IpAddrRangeRequest> listIpAddrRange() {
        LOG.debug("List ip address range");

        Map<String, IpAddrRange> ipAddrRangeMap = ipAddrRangeRepo.findAllItems();

        List<IpAddrRangeRequest> result = new ArrayList<>();
        ipAddrRangeMap.forEach((k,v) -> {
            IpAddrRangeRequest range = new IpAddrRangeRequest();
            range.setSubnetId(v.getSubnetId());
            range.setFirstAddr(v.getFirstAddr());
            range.setLastAddr(v.getLastAddr());
            result.add(range);
        });

        LOG.info("List ip address range success, result: {}", result);

        return result;
    }
}
