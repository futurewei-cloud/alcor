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

package com.futurewei.alcor.privateipmanager.controller;

import com.futurewei.alcor.privateipmanager.entity.IpAddrState;
import com.futurewei.alcor.privateipmanager.entity.IpVersion;
import com.futurewei.alcor.privateipmanager.entity.IpAddrRequest;
import com.futurewei.alcor.privateipmanager.entity.IpAddrRangeRequest;
import com.futurewei.alcor.privateipmanager.entity.IpAddrRequestBulk;
import com.futurewei.alcor.privateipmanager.exception.*;
import com.futurewei.alcor.privateipmanager.service.implement.IpAddrServiceImpl;
import com.futurewei.alcor.privateipmanager.utils.Ipv4AddrUtil;
import com.futurewei.alcor.privateipmanager.utils.Ipv6AddrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;


@RestController
public class IpAddrController {
    @Autowired
    IpAddrServiceImpl ipAddrService;

    private void checkRangeId(String rangeId) throws IpRangeIdInvalidException {
        if (rangeId == null || "".equals(rangeId)) {
            throw new IpRangeIdInvalidException();
        }
    }

    private void checkSubnetId(String subnetId) throws SubnetIdInvalidException {
        if (subnetId == null || "".equals(subnetId)) {
            throw new SubnetIdInvalidException();
        }
    }

    private void checkIpAddr(String ipAddr, int ipVersion) throws Exception {
        if (ipVersion == IpVersion.IPV4.getVersion()) {
            if (!Ipv4AddrUtil.formatCheck(ipAddr)) {
                throw new IpAddrInvalidException();
            }
        } else if (ipVersion == IpVersion.IPV6.getVersion()) {
            if (!Ipv6AddrUtil.formatCheck(ipAddr)) {
                throw new IpAddrInvalidException();
            }
        } else {
            throw new IpAddrInvalidException();
        }
    }

    private void checkIpAddrState(String state) throws IpAddrStateInvalidException {
        if (!IpAddrState.ACTIVATED.getState().equals(state) &&
                !IpAddrState.DEACTIVATED.getState().equals(state)) {
            throw new IpAddrStateInvalidException();
        }
    }

    private void checkIpVersion(int ipVersion) throws Exception {
        if (ipVersion != IpVersion.IPV4.getVersion() && ipVersion != IpVersion.IPV6.getVersion()) {
            throw new IpVersionInvalidException();
        }
    }

    @PostMapping("/ips")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public IpAddrRequest allocateIpAddr(@RequestBody IpAddrRequest request) throws Exception {
        checkRangeId(request.getRangeId());

        return ipAddrService.allocateIpAddr(request);
    }

    @PostMapping("/ips/bulk")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public IpAddrRequestBulk allocateIpAddrBulk(@RequestBody IpAddrRequestBulk requestBulk) throws Exception {
        for (IpAddrRequest request : requestBulk.getIpRequests()) {
            checkRangeId(request.getRangeId());
        }

        return ipAddrService.allocateIpAddrBulk(requestBulk);
    }

    @PutMapping("/ips")
    @ResponseBody
    public IpAddrRequest modifyIpAddrState(@RequestBody IpAddrRequest request) throws Exception {
        checkRangeId(request.getRangeId());
        checkIpAddr(request.getIp(), request.getIpVersion());
        checkIpAddrState(request.getState());

        return ipAddrService.modifyIpAddrState(request);
    }

    @PutMapping("/ips/bulk")
    @ResponseBody
    public IpAddrRequestBulk modifyIpAddrStateBulk(@RequestBody IpAddrRequestBulk requestBulk) throws Exception {
        for (IpAddrRequest request : requestBulk.getIpRequests()) {
            checkRangeId(request.getRangeId());
            checkRangeId(request.getIp());
            checkRangeId(request.getState());
        }

        return ipAddrService.modifyIpAddrStateBulk(requestBulk);
    }

    @DeleteMapping("/ips/{ip_version}/{range_id}/{ip}")
    @ResponseBody
    public IpAddrRequest releaseIpAddr(@PathVariable("ip_version") int ipVersion,
                                         @PathVariable("range_id") String rangeId,
                                         @PathVariable("ip") String ipAddr) throws Exception {
        checkRangeId(rangeId);
        checkIpAddr(ipAddr, ipVersion);

        return ipAddrService.releaseIpAddr(ipVersion, rangeId, ipAddr);
    }

    @DeleteMapping("/ips/bulk")
    @ResponseBody
    public IpAddrRequestBulk releaseIpAddrBulk(@RequestBody IpAddrRequestBulk requestBulk) throws Exception {
        for (IpAddrRequest request : requestBulk.getIpRequests()) {
            checkRangeId(request.getRangeId());
            checkIpAddr(request.getIp(), request.getIpVersion());
        }

        return ipAddrService.releaseIpAddrBulk(requestBulk);
    }

    @GetMapping("/ips/{ip_version}/{range_id}/{ip}")
    @ResponseBody
    public IpAddrRequest getIpAddr(@PathVariable("ip_version") int ipVersion,
                                     @PathVariable("range_id") String rangeId,
                                     @PathVariable("ip") String ipAddr) throws Exception {
        checkRangeId(rangeId);
        checkIpAddr(ipAddr, ipVersion);

        return ipAddrService.getIpAddr(ipVersion, rangeId, ipAddr);
    }

    @GetMapping("/ips/{ip_version}/{range_id}")
    @ResponseBody
    public List<IpAddrRequest> getIpAddrBulk(@PathVariable("ip_version") int ipVersion, @PathVariable("range_id") String rangeId) throws Exception {
        checkIpVersion(ipVersion);
        return ipAddrService.getIpAddrBulk(rangeId);
    }

    @PostMapping("/ips/range")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public IpAddrRangeRequest createIpAddrRange(@RequestBody IpAddrRangeRequest request) throws Exception {
        checkRangeId(request.getId());
        checkSubnetId(request.getSubnetId());
        checkIpAddr(request.getFirstIp(), request.getIpVersion());
        checkIpAddr(request.getLastIp(), request.getIpVersion());

        //Check if first < last
        if (request.getIpVersion() == IpVersion.IPV4.getVersion()) {
            long firstIpLong = Ipv4AddrUtil.ipv4ToLong(request.getFirstIp());
            long lastIpLong = Ipv4AddrUtil.ipv4ToLong(request.getLastIp());
            if (firstIpLong >= lastIpLong) {
                throw new IpAddrRangeInvalidException();
            }
        } else {
            BigInteger firstIpBigInt = Ipv6AddrUtil.ipv6ToBitInt(request.getFirstIp());
            BigInteger lastIpBigInt = Ipv6AddrUtil.ipv6ToBitInt(request.getLastIp());
            if (firstIpBigInt.compareTo(lastIpBigInt) > 0) {
                throw new IpAddrRangeInvalidException();
            }
        }

        return ipAddrService.createIpAddrRange(request);
    }

    @DeleteMapping("/ips/range/{range_id}")
    @ResponseBody
    public IpAddrRangeRequest deleteIpAddrRange(@PathVariable("range_id") String rangeId) throws Exception {
        return ipAddrService.deleteIpAddrRange(rangeId);
    }

    @GetMapping("/ips/range/{range_id}")
    @ResponseBody
    public IpAddrRangeRequest getIpAddrRange(@PathVariable("range_id") String rangeId) throws Exception {
        checkRangeId(rangeId);

        return ipAddrService.getIpAddrRange(rangeId);
    }

    @GetMapping("/ips/range")
    @ResponseBody
    public List<IpAddrRangeRequest> listIpAddrRange() {
        return ipAddrService.listIpAddrRange();
    }
}
