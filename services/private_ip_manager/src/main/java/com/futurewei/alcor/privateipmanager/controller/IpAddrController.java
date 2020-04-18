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
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class IpAddrController {
    @Autowired
    IpAddrServiceImpl ipAddrService;

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
    public IpAddrRequest allocateIpAddr(@RequestBody IpAddrRequest request) throws Exception {
        checkSubnetId(request.getSubnetId());

        return ipAddrService.allocateIpAddr(request);
    }

    @PostMapping("/ips/bulk")
    @ResponseBody
    public IpAddrRequestBulk allocateIpAddrBulk(@RequestBody IpAddrRequestBulk requestBulk) throws Exception {
        for (IpAddrRequest request : requestBulk.getIpAddrRequests()) {
            checkSubnetId(request.getSubnetId());
        }

        return ipAddrService.allocateIpAddrBulk(requestBulk);
    }

    @PutMapping("/ips")
    @ResponseBody
    public IpAddrRequest modifyIpAddrState(@RequestBody IpAddrRequest request) throws Exception {
        checkSubnetId(request.getSubnetId());
        checkIpAddr(request.getIpAddr(), request.getIpVersion());
        checkIpAddrState(request.getState());

        return ipAddrService.modifyIpAddrState(request);
    }

    @PutMapping("/ips/bulk")
    @ResponseBody
    public IpAddrRequestBulk modifyIpAddrStateBulk(@RequestBody IpAddrRequestBulk requestBulk) throws Exception {
        for (IpAddrRequest request : requestBulk.getIpAddrRequests()) {
            checkSubnetId(request.getSubnetId());
            checkSubnetId(request.getIpAddr());
            checkSubnetId(request.getState());
        }

        return ipAddrService.modifyIpAddrStateBulk(requestBulk);
    }

    @DeleteMapping("/ips/{ip_version}/{subnet_id}/{ip}")
    @ResponseBody
    public IpAddrRequest releaseIpAddr(@PathVariable("ip_version") int ipVersion,
                                         @PathVariable("subnet_id") String subnetId,
                                         @PathVariable("ip") String ipAddr) throws Exception {
        checkSubnetId(subnetId);
        checkIpAddr(ipAddr, ipVersion);

        return ipAddrService.releaseIpAddr(ipVersion, subnetId, ipAddr);
    }

    @DeleteMapping("/ips/bulk")
    @ResponseBody
    public IpAddrRequestBulk releaseIpAddrBulk(@RequestBody IpAddrRequestBulk requestBulk) throws Exception {
        for (IpAddrRequest request : requestBulk.getIpAddrRequests()) {
            checkSubnetId(request.getSubnetId());
        }

        return ipAddrService.releaseIpAddrBulk(requestBulk);
    }

    @GetMapping("/ips/{ip_version}/{subnet_id}/{ip}")
    @ResponseBody
    public IpAddrRequest getIpAddr(@PathVariable("ip_version") int ipVersion,
                                     @PathVariable("subnet_id") String subnetId,
                                     @PathVariable("ip") String ipAddr) throws Exception {
        checkSubnetId(subnetId);
        checkIpAddr(ipAddr, ipVersion);

        return ipAddrService.getIpAddr(ipVersion, subnetId, ipAddr);
    }

    @GetMapping("/ips/{ip_version}/{subnet_id}")
    @ResponseBody
    public List<IpAddrRequest> getIpAddrBulk(@PathVariable("ip_version") int ipVersion, @PathVariable("subnet_id") String subnetId) throws Exception {
        checkIpVersion(ipVersion);
        return ipAddrService.getIpAddrBulk(subnetId);
    }

    @PostMapping("/ips/range")
    @ResponseBody
    public IpAddrRangeRequest createIpAddrRange(@RequestBody IpAddrRangeRequest request) throws Exception {
        checkSubnetId(request.getSubnetId());
        checkIpAddr(request.getFirstAddr(), request.getIpVersion());
        checkIpAddr(request.getLastAddr(), request.getIpVersion());

        //Check if first < last
        long firstAddrLong = Ipv4AddrUtil.ipv4ToLong(request.getFirstAddr());
        long lastAddrLong = Ipv4AddrUtil.ipv4ToLong(request.getLastAddr());
        if (firstAddrLong >= lastAddrLong) {
            throw new IpAddrRangeInvalidException();
        }

        return ipAddrService.createIpAddrRange(request);
    }

    @DeleteMapping("/ips/range/{subnet_id}")
    @ResponseBody
    public IpAddrRangeRequest deleteIpAddrRange(@PathVariable("subnet_id") String subnetId) throws Exception {
        return ipAddrService.deleteIpAddrRange(subnetId);
    }

    @GetMapping("/ips/range/{subnet_id}")
    @ResponseBody
    public IpAddrRangeRequest getIpAddrRange(@PathVariable("subnet_id") String subnetId) throws Exception {
        checkSubnetId(subnetId);

        return ipAddrService.getIpAddrRange(subnetId);
    }

    @GetMapping("/ips/range")
    @ResponseBody
    public List<IpAddrRangeRequest> listIpAddrRange() {
        return ipAddrService.listIpAddrRange();
    }
}
