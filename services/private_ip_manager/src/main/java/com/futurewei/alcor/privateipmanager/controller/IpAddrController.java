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

import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.ip.*;
import com.futurewei.alcor.privateipmanager.exception.*;
import com.futurewei.alcor.privateipmanager.service.implement.IpAddrServiceImpl;
import com.futurewei.alcor.privateipmanager.utils.Ipv4AddrUtil;
import com.futurewei.alcor.privateipmanager.utils.Ipv6AddrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;


@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class IpAddrController {
    @Autowired
    IpAddrServiceImpl ipAddrService;

    private void checkVpcId(String vpcId) throws VpcIdInvalidException {
        if (vpcId == null || "".equals(vpcId)) {
            throw new VpcIdInvalidException();
        }
    }

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

    private void checkIpAddr(String ipAddr) throws Exception {
        checkIpVersion(ipAddr);
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

    private int checkIpVersion(String ipAddress) throws Exception {
        if (Ipv4AddrUtil.formatCheck(ipAddress)) {
            return IpVersion.IPV4.getVersion();
        } else if (Ipv6AddrUtil.formatCheck(ipAddress)) {
            return IpVersion.IPV6.getVersion();
        } else {
            throw new IpAddrInvalidException();
        }
    }

    @PostMapping("/ips")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public IpAddrRequest allocateIpAddr(@RequestBody IpAddrRequest request) throws Exception {
        if (request.getVpcId() == null && request.getRangeId() == null) {
            throw new IpRangeIdInvalidException();
        }

        if (request.getVpcId() != null) {
            checkVpcId(request.getVpcId());
            checkIpVersion(request.getIpVersion());
        }

        if (request.getRangeId() != null) {
            checkRangeId(request.getRangeId());

            if (request.getIp() != null) {
                checkIpAddr(request.getIp());
            }
        }

        return ipAddrService.allocateIpAddr(request);
    }

    @PostMapping("/ips/bulk")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public IpAddrRequestBulk allocateIpAddrBulk(@RequestBody IpAddrRequestBulk requestBulk) throws Exception {
        for (IpAddrRequest request : requestBulk.getIpRequests()) {
            checkRangeId(request.getRangeId());
        }

        return ipAddrService.allocateIpAddrBulk(requestBulk);
    }

    @PutMapping("/ips")
    @ResponseBody
    @DurationStatistics
    public IpAddrRequest modifyIpAddrState(@RequestBody IpAddrRequest request) throws Exception {
        checkRangeId(request.getRangeId());
        checkIpAddr(request.getIp());
        checkIpAddrState(request.getState());

        return ipAddrService.modifyIpAddrState(request);
    }

    @PutMapping("/ips/bulk")
    @ResponseBody
    @DurationStatistics
    public IpAddrRequestBulk modifyIpAddrStateBulk(@RequestBody IpAddrRequestBulk requestBulk) throws Exception {
        for (IpAddrRequest request : requestBulk.getIpRequests()) {
            checkRangeId(request.getRangeId());
            checkRangeId(request.getIp());
            checkRangeId(request.getState());
        }

        return ipAddrService.modifyIpAddrStateBulk(requestBulk);
    }

    @DeleteMapping("/ips/{range_id}/{ip}")
    @ResponseBody
    @DurationStatistics
    public void releaseIpAddr(@PathVariable("range_id") String rangeId,
                              @PathVariable("ip") String ipAddr) throws Exception {
        checkRangeId(rangeId);
        checkIpAddr(ipAddr);

        ipAddrService.releaseIpAddr(rangeId, ipAddr);
    }

    @DeleteMapping("/ips/bulk")
    @ResponseBody
    @DurationStatistics
    public void releaseIpAddrBulk(@RequestBody IpAddrRequestBulk requestBulk) throws Exception {
        for (IpAddrRequest request : requestBulk.getIpRequests()) {
            checkRangeId(request.getRangeId());
            checkIpAddr(request.getIp());
        }

        ipAddrService.releaseIpAddrBulk(requestBulk);
    }

    @GetMapping("/ips/{range_id}/{ip}")
    @ResponseBody
    @DurationStatistics
    public IpAddrRequest getIpAddr(@PathVariable("range_id") String rangeId,
                                   @PathVariable("ip") String ipAddr) throws Exception {
        checkRangeId(rangeId);
        checkIpAddr(ipAddr);

        return ipAddrService.getIpAddr(rangeId, ipAddr);
    }

    @GetMapping("/ips/{range_id}")
    @ResponseBody
    @DurationStatistics
    public List<IpAddrRequest> getIpAddrBulk(@PathVariable("range_id") String rangeId) throws Exception {
        return ipAddrService.getIpAddrBulk(rangeId);
    }

    @PostMapping("/ips/range")
    @ResponseBody
    @DurationStatistics
    @ResponseStatus(HttpStatus.CREATED)
    public IpAddrRangeRequest createIpAddrRange(@RequestBody IpAddrRangeRequest request) throws Exception {
        checkVpcId(request.getVpcId());
        checkSubnetId(request.getSubnetId());
        checkIpAddr(request.getFirstIp());
        checkIpAddr(request.getLastIp());
        checkIpVersion(request.getIpVersion());

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
    @DurationStatistics
    public void deleteIpAddrRange(@PathVariable("range_id") String rangeId) throws Exception {
        ipAddrService.deleteIpAddrRange(rangeId);
    }

    @GetMapping("/ips/range/{range_id}")
    @ResponseBody
    @DurationStatistics
    public IpAddrRangeRequest getIpAddrRange(@PathVariable("range_id") String rangeId) throws Exception {
        checkRangeId(rangeId);

        return ipAddrService.getIpAddrRange(rangeId);
    }

    @GetMapping("/ips/range")
    @ResponseBody
    @DurationStatistics
    public List<IpAddrRangeRequest> listIpAddrRange() {
        return ipAddrService.listIpAddrRange();
    }
}
