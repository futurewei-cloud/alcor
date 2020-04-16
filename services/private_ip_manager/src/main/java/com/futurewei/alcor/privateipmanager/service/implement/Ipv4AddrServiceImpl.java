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

import com.futurewei.alcor.privateipmanager.http.Ipv4AddrRequest;
import com.futurewei.alcor.privateipmanager.http.Ipv4AddrRangeRequest;
import com.futurewei.alcor.privateipmanager.entity.Ipv4AddrState;
import com.futurewei.alcor.privateipmanager.entity.Ipv4AddrAlloc;
import com.futurewei.alcor.privateipmanager.entity.Ipv4AddrRange;
import com.futurewei.alcor.privateipmanager.http.Ipv4AddrRequestBulk;
import com.futurewei.alcor.privateipmanager.http.status.*;
import com.futurewei.alcor.privateipmanager.repo.Ipv4AddrRangeRepo;
import com.futurewei.alcor.privateipmanager.repo.Ipv4AddrRepo;
import com.futurewei.alcor.privateipmanager.service.Ipv4AddrService;
import com.futurewei.alcor.privateipmanager.utils.Ipv4AddrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class Ipv4AddrServiceImpl implements Ipv4AddrService {
    private static final Logger LOG = LoggerFactory.getLogger(Ipv4AddrServiceImpl.class);

    @Autowired
    Ipv4AddrRepo ipv4AddrRepo;

    @Autowired
    Ipv4AddrRangeRepo ipv4AddrRangeRepo;

    private List<String> getRangeAvailableIps(Ipv4AddrRange ipv4AddrRange) {
        List<String> availableIps = new ArrayList<>();

        Set<String> rangeIps = new HashSet<>();

        long firsAddrLong = Ipv4AddrUtil.ipToLong(ipv4AddrRange.getFirstAddr());
        long lastAddrLong = Ipv4AddrUtil.ipToLong(ipv4AddrRange.getLastAddr());

        long i = firsAddrLong;
        while (i <= lastAddrLong) {
            rangeIps.add(Ipv4AddrUtil.longToIp(i));
            i++;
        }

        String subnetId = ipv4AddrRange.getSubnetId();
        Map<String, Ipv4AddrAlloc> ipv4AddressAllocMap = ipv4AddrRepo.findAllItems();

        Set<String> allocatedIps = ipv4AddressAllocMap.values().stream()
                .filter(alloc -> alloc.getSubnetId().equals(subnetId))
                .map(alloc -> alloc.getIpv4Addr())
                .collect(Collectors.toSet());

        if (allocatedIps.size() > (lastAddrLong - firsAddrLong)) {
            return availableIps;
        }

        availableIps = rangeIps.stream()
                .filter(ip -> !allocatedIps.contains(ip))
                .collect(Collectors.toList());

        return availableIps;
    }

    public Ipv4AddrRequest allocateIpv4Addr(Ipv4AddrRequest request) throws Exception {
        LOG.debug("Allocate ipv4 address, request: {}", request);

        Ipv4AddrRange ipv4AddrRange = ipv4AddrRangeRepo.findItem(request.getSubnetId());
        if (ipv4AddrRange == null) {
            LOG.error("Allocate ipv4 address failed: Ipv4 address range not found");
            throw new Ipv4AddrRangeNotFoundException();
        }

        List<String> availableIps = getRangeAvailableIps(ipv4AddrRange);
        if (availableIps.size() == 0) {
            LOG.error("Allocate ipv4 address failed: No available ipv4 address");
            throw new Ipv4AddrNotEnoughException();
        }

        int index = new Random().ints(0, (availableIps.size()))
                .limit(1).findFirst().getAsInt();

        String ipv4Addr = availableIps.get(index);

        Ipv4AddrAlloc ipv4AddrAlloc = new Ipv4AddrAlloc();
        ipv4AddrAlloc.setSubnetId(request.getSubnetId());
        ipv4AddrAlloc.setIpv4Addr(ipv4Addr);
        ipv4AddrAlloc.setState(Ipv4AddrState.ACTIVATED.getState());

        ipv4AddrRepo.addItem(ipv4AddrAlloc);
        String key = ipv4AddrAlloc.getSubnetId() + ipv4AddrAlloc.getIpv4Addr();
        if (ipv4AddrRepo.findItem(key) == null) {
            LOG.error("Allocate ipv4 address failed: Internal db operation error");
            throw new InternalDbOperationException();
        }

        LOG.info("Allocate ipv4 address success, ipv4AddrAlloc: {}", ipv4AddrAlloc);

        request.setIpv4Addr(ipv4Addr);
        request.setState(Ipv4AddrState.ACTIVATED.getState());

        return request;
    }

    private List<Ipv4AddrRequest> allocateIpv4AddrFromRange(String subnetId, long requestNum) {
        List<Ipv4AddrRequest> ipv4AddrRequests = new ArrayList<>();

        Ipv4AddrRange ipv4AddrRange = ipv4AddrRangeRepo.findItem(subnetId);
        if (ipv4AddrRange == null) {
            LOG.warn("Allocate ipv4 address from range failed: Ipv4 address range not found");
            return ipv4AddrRequests;
        }

        List<String> availableIps = getRangeAvailableIps(ipv4AddrRange);

        if (availableIps.size() == 0) {
            LOG.warn("Allocate ipv4 address from range failed: No available ipv4 address");
            return ipv4AddrRequests;
        }

        for (String ipv4Addr: availableIps) {
            if (requestNum <= 0) {
                break;
            }

            Ipv4AddrAlloc ipv4AddrAlloc = new Ipv4AddrAlloc();
            ipv4AddrAlloc.setSubnetId(subnetId);
            ipv4AddrAlloc.setIpv4Addr(ipv4Addr);
            ipv4AddrAlloc.setState(Ipv4AddrState.ACTIVATED.getState());

            ipv4AddrRepo.addItem(ipv4AddrAlloc);
            if (ipv4AddrRepo.findItem(subnetId + ipv4Addr) == null) {
                LOG.warn("Allocate ipv4 address from range failed: Internal db operation error");
                break;
            }

            Ipv4AddrRequest ipv4AddrRequest = new Ipv4AddrRequest();
            ipv4AddrRequest.setSubnetId(subnetId);
            ipv4AddrRequest.setIpv4Addr(ipv4Addr);
            ipv4AddrRequest.setState(Ipv4AddrState.ACTIVATED.getState());
            ipv4AddrRequests.add(ipv4AddrRequest);

            requestNum--;
        }

        return ipv4AddrRequests;
    }

    public Ipv4AddrRequestBulk allocateIpv4AddrBulk(Ipv4AddrRequestBulk requestBulk) {
        LOG.debug("Allocate ipv4 address bulk, requestBulk: {}", requestBulk);

        Map<String, Integer> subnetAddrNumMap = new HashMap<>();
        List<Ipv4AddrRequest> ipv4AddrRequests = new ArrayList<>();

        for (Ipv4AddrRequest request : requestBulk.getIpv4AddrRequests()) {
            Integer num = 1;

            if (subnetAddrNumMap.containsKey(request.getSubnetId())) {
                num = subnetAddrNumMap.get(request.getSubnetId()) + 1;
            }

            subnetAddrNumMap.put(request.getSubnetId(), num);
        }

        for (Map.Entry<String, Integer> entry: subnetAddrNumMap.entrySet()) {
            ipv4AddrRequests.addAll(allocateIpv4AddrFromRange(entry.getKey(), entry.getValue()));
        }

        requestBulk.setIpv4AddrRequests(ipv4AddrRequests);

        LOG.info("Allocate ipv4 address bulk done, requestBulk: {}", requestBulk);

        return requestBulk;
    }

    public Ipv4AddrRequest modifyIpv4AddrState(Ipv4AddrRequest request) throws Exception {
        LOG.debug("Modify ipv4 address state, request: {}", request);

        String key = request.getSubnetId() + request.getIpv4Addr();

        Ipv4AddrAlloc ipv4AddrAlloc = ipv4AddrRepo.findItem(key);
        if (ipv4AddrAlloc == null) {
            LOG.warn("Modify ipv4 address state failed: Ipv4 address allocation not found");
            throw new Ipv4AddrAllocNotFoundException();
        }

        if (!request.getState().equals(ipv4AddrAlloc.getState())) {
            ipv4AddrAlloc.setState(request.getState());
            ipv4AddrRepo.addItem(ipv4AddrAlloc);

            ipv4AddrAlloc = ipv4AddrRepo.findItem(key);
            if (ipv4AddrAlloc == null || !request.getState().equals(ipv4AddrAlloc.getState())) {
                LOG.warn("Modify ipv4 address state failed: Internal db operation error");
                throw new InternalDbOperationException();
            }
        }

        LOG.info("Modify ipv4 address state success, request: {}", request);

        return request;
    }

    public Ipv4AddrRequestBulk modifyIpv4AddrStateBulk(Ipv4AddrRequestBulk requestBulk) {
        LOG.debug("Modify ipv4 address state bulk, requestBulk: {}", requestBulk);

        List<Ipv4AddrRequest> ipv4AddrRequests = new ArrayList<>();
        for (Ipv4AddrRequest request: requestBulk.getIpv4AddrRequests()) {
            try {
                ipv4AddrRequests.add(modifyIpv4AddrState(request));
            } catch (Exception e) {
                LOG.warn("Modify ipv4 address state failed, request: {}", request);
            }
        }

        requestBulk.setIpv4AddrRequests(ipv4AddrRequests);

        LOG.info("Modify ipv4 address state bulk done, requestBulk: {}", requestBulk);

        return requestBulk;
    }

    public Ipv4AddrRequest releaseIpv4Addr(String subnetId, String ipv4Addr) throws Exception {
        LOG.debug("Release ipv4 address, ipv4Addr: {}", ipv4Addr);

        String key = subnetId + ipv4Addr;

        Ipv4AddrAlloc ipv4AddrAlloc = ipv4AddrRepo.findItem(key);
        if (ipv4AddrAlloc == null) {
            LOG.warn("Release ipv4 address failed: Ipv4 address allocation not found");
            throw new Ipv4AddrAllocNotFoundException();
        }

        ipv4AddrRepo.deleteItem(key);

        Ipv4AddrRequest result = new Ipv4AddrRequest();
        result.setSubnetId(ipv4AddrAlloc.getSubnetId());
        result.setIpv4Addr(ipv4AddrAlloc.getIpv4Addr());
        result.setState(Ipv4AddrState.FREE.getState());

        LOG.info("Release ipv4 address success, ipv4AddrAlloc: {}", ipv4AddrAlloc);

        return result;
    }

    public Ipv4AddrRequestBulk releaseIpv4AddrBulk(Ipv4AddrRequestBulk requestBulk) {
        LOG.debug("Release ipv4 address bulk, requestBulk: {}", requestBulk);

        List<Ipv4AddrRequest> ipv4AddrRequests = new ArrayList<>();
        for (Ipv4AddrRequest request: requestBulk.getIpv4AddrRequests()) {
            try {
                ipv4AddrRequests.add(releaseIpv4Addr(request.getSubnetId(), request.getIpv4Addr()));
            } catch (Exception e) {
                LOG.warn("Release ipv4 address failed, request: {}", request);
            }
        }

        requestBulk.setIpv4AddrRequests(ipv4AddrRequests);

        LOG.info("Release ipv4 address bulk done, requestBulk: {}", requestBulk);

        return requestBulk;
    }

    public Ipv4AddrRequest getIpv4Addr(String subnetId, String ipv4Addr) throws Exception {
        LOG.debug("Get ipv4 address, subnetId: {}, ipv4Addr: {}", subnetId, ipv4Addr);

        String key = subnetId + ipv4Addr;
        Ipv4AddrAlloc ipv4AddrAlloc = ipv4AddrRepo.findItem(key);
        if (ipv4AddrAlloc == null) {
            Ipv4AddrRange ipv4AddrRange = ipv4AddrRangeRepo.findItem(subnetId);
            if (ipv4AddrRange == null) {
                LOG.warn("Get ipv4 address failed: Ipv4 address not found");
                throw new Ipv4AddrNotFoundException();
            }

            long ipv4AddrLong = Ipv4AddrUtil.ipToLong(ipv4Addr);
            long firstAddrLong = Ipv4AddrUtil.ipToLong(ipv4AddrRange.getFirstAddr());
            long lastAddrLong = Ipv4AddrUtil.ipToLong(ipv4AddrRange.getLastAddr());
            if (ipv4AddrLong < firstAddrLong || ipv4AddrLong > lastAddrLong) {
                LOG.warn("Get ipv4 address failed: Ipv4 address not found");
                throw new Ipv4AddrNotFoundException();
            }
        }

        Ipv4AddrRequest result = new Ipv4AddrRequest();

        if (ipv4AddrAlloc != null) {
            result.setSubnetId(ipv4AddrAlloc.getSubnetId());
            result.setIpv4Addr(ipv4AddrAlloc.getIpv4Addr());
            result.setState(ipv4AddrAlloc.getState());
        } else {
            result.setSubnetId(subnetId);
            result.setIpv4Addr(ipv4Addr);
            result.setState(Ipv4AddrState.FREE.getState());
        }

        LOG.info("Get ipv4 address success, result: {}", result);

        return result;
    }

    public List<Ipv4AddrRequest> listAllocatedIpv4Addr() {
        LOG.debug("List ipv4 address");

        Map<String, Ipv4AddrAlloc> ipv4AddrMap = ipv4AddrRepo.findAllItems();

        List<Ipv4AddrRequest> result = new ArrayList<>();

        ipv4AddrMap.forEach((k,v) -> {
            Ipv4AddrRequest ipv4Addr = new Ipv4AddrRequest();
            ipv4Addr.setSubnetId(v.getSubnetId());
            ipv4Addr.setIpv4Addr(v.getIpv4Addr());
            ipv4Addr.setState(v.getState());
            result.add(ipv4Addr);
        });

        LOG.info("List ipv4 address success, result: {}", result);

        return result;
    }

    public Ipv4AddrRangeRequest createIpv4AddrRange(Ipv4AddrRangeRequest request) throws Exception {
        LOG.debug("Create ipv4 address range, request: {}", request);

        if (ipv4AddrRangeRepo.findItem(request.getSubnetId()) != null) {
            LOG.warn("Create ipv4 address range failed: Ipv4AddressRange already exists");
            throw new Ipv4AddrRangeExistException();
        }

        Ipv4AddrRange ipv4AddrRange = new Ipv4AddrRange();
        ipv4AddrRange.setSubnetId(request.getSubnetId());
        ipv4AddrRange.setFirstAddr(request.getFirstAddr());
        ipv4AddrRange.setLastAddr(request.getLastAddr());

        ipv4AddrRangeRepo.addItem(ipv4AddrRange);

        ipv4AddrRange = ipv4AddrRangeRepo.findItem(request.getSubnetId());
        if (ipv4AddrRange == null) {
            LOG.warn("Create ipv4 address range failed: Internal db operation error");
            throw new InternalDbOperationException();
        }

        LOG.info("Create ipv4 address range success, ipv4AddrRange: {}", ipv4AddrRange);

        return request;
    }

    public Ipv4AddrRangeRequest deleteIpv4AddrRange(String subnetId) throws Exception {
        LOG.debug("Delete ipv4 address range, subnetId: {}", subnetId);

        Ipv4AddrRange ipv4AddrRange = ipv4AddrRangeRepo.findItem(subnetId);
        if (ipv4AddrRange == null) {
            LOG.warn("Delete ipv4 address range failed: Ipv4 address range not found");
            throw new Ipv4AddrRangeNotFoundException();
        }

        ipv4AddrRangeRepo.deleteItem(subnetId);

        Ipv4AddrRangeRequest request = new Ipv4AddrRangeRequest();
        request.setSubnetId(ipv4AddrRange.getSubnetId());
        request.setFirstAddr(ipv4AddrRange.getFirstAddr());
        request.setLastAddr(ipv4AddrRange.getLastAddr());

        LOG.info("Delete ipv4 address range success, request: {}", request);

        return request;
    }

    public Ipv4AddrRangeRequest getIpv4AddrRange(String subnetId) throws Exception {
        LOG.debug("Get ipv4 address range, subnetId: {}", subnetId);

        Ipv4AddrRange ipv4AddrRange = ipv4AddrRangeRepo.findItem(subnetId);
        if (ipv4AddrRange == null) {
            throw new Ipv4AddrRangeNotFoundException();
        }

        LOG.info("Get ipv4 address range success, ipv4AddressRange: {}", ipv4AddrRange);

        Ipv4AddrRangeRequest result = new Ipv4AddrRangeRequest();
        result.setSubnetId(ipv4AddrRange.getSubnetId());
        result.setFirstAddr(ipv4AddrRange.getFirstAddr());
        result.setLastAddr(ipv4AddrRange.getLastAddr());

        return result;
    }

    public List<Ipv4AddrRangeRequest> listIpv4AddrRange() {
        LOG.debug("List ipv4 address range");

        Map<String, Ipv4AddrRange> ipv4AddrRangeMap = ipv4AddrRangeRepo.findAllItems();

        List<Ipv4AddrRangeRequest> result = new ArrayList<>();
        ipv4AddrRangeMap.forEach((k,v) -> {
            Ipv4AddrRangeRequest range = new Ipv4AddrRangeRequest();
            range.setSubnetId(v.getSubnetId());
            range.setFirstAddr(v.getFirstAddr());
            range.setLastAddr(v.getLastAddr());
            result.add(range);
        });

        LOG.info("List ipv4 address range success, result: {}", result);

        return result;
    }
}
