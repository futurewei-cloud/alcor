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
import com.futurewei.alcor.privateipmanager.entity.Ipv4AddrState;
import com.futurewei.alcor.privateipmanager.http.Ipv4AddrRequest;
import com.futurewei.alcor.privateipmanager.http.Ipv4AddrRangeRequest;
import com.futurewei.alcor.privateipmanager.http.Ipv4AddrRequestBulk;
import com.futurewei.alcor.privateipmanager.http.status.Ipv4AddrInvalidException;
import com.futurewei.alcor.privateipmanager.http.status.Ipv4AddrRangeInvalidException;
import com.futurewei.alcor.privateipmanager.http.status.Ipv4AddrStateInvalidException;
import com.futurewei.alcor.privateipmanager.http.status.SubnetIdInvalidException;
import com.futurewei.alcor.privateipmanager.service.implement.Ipv4AddrServiceImpl;
import com.futurewei.alcor.privateipmanager.utils.Ipv4AddrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
public class Ipv4AddrController {
    @Autowired
    Ipv4AddrServiceImpl ipv4AddrServiceImpl;

    private void checkSubnetId(String subnetId) throws SubnetIdInvalidException {
        if (subnetId == null || "".equals(subnetId)) {
            throw new SubnetIdInvalidException();
        }
    }

    private void checkIpv4Addr(String ipv4Addr) throws Ipv4AddrInvalidException {
        if (!Ipv4AddrUtil.formatCheck(ipv4Addr)) {
            throw new Ipv4AddrInvalidException();
        }
    }

    private void checkIpv4AddrState(String state) throws Ipv4AddrStateInvalidException {
        if (!Ipv4AddrState.ACTIVATED.getState().equals(state) &&
                !Ipv4AddrState.DEACTIVATED.getState().equals(state)) {
            throw new Ipv4AddrStateInvalidException();
        }
    }

    @PostMapping("/v4/ips")
    @ResponseBody
    public Ipv4AddrRequest allocateIpv4Addr(@RequestBody Ipv4AddrRequest request) throws Exception {
        checkSubnetId(request.getSubnetId());

        return ipv4AddrServiceImpl.allocateIpv4Addr(request);
    }

    @PostMapping("/v4/ips/bulk")
    @ResponseBody
    public Ipv4AddrRequestBulk allocateIpv4AddrBulk(@RequestBody Ipv4AddrRequestBulk requestBulk) throws Exception {
        for(Ipv4AddrRequest request : requestBulk.getIpv4AddrRequests()) {
            checkSubnetId(request.getSubnetId());
        }

        return ipv4AddrServiceImpl.allocateIpv4AddrBulk(requestBulk);
    }

    @PutMapping("/v4/ips")
    @ResponseBody
    public Ipv4AddrRequest modifyIpv4AddrState(@RequestBody Ipv4AddrRequest request) throws Exception {
        checkSubnetId(request.getSubnetId());
        checkIpv4Addr(request.getIpv4Addr());
        checkIpv4AddrState(request.getState());

        return ipv4AddrServiceImpl.modifyIpv4AddrState(request);
    }

    @PutMapping("/v4/ips/bulk")
    @ResponseBody
    public Ipv4AddrRequestBulk modifyIpv4AddrStateBulk(@RequestBody Ipv4AddrRequestBulk requestBulk) throws Exception {
        for(Ipv4AddrRequest request : requestBulk.getIpv4AddrRequests()) {
            checkSubnetId(request.getSubnetId());
            checkSubnetId(request.getIpv4Addr());
            checkSubnetId(request.getState());
        }

        return ipv4AddrServiceImpl.modifyIpv4AddrStateBulk(requestBulk);
    }

    @DeleteMapping("/v4/ips/{subnet_id}/{ip}")
    @ResponseBody
    public Ipv4AddrRequest releaseIpv4Addr(@PathVariable("subnet_id") String subnetId, @PathVariable("ip") String ipv4Addr) throws Exception {
        checkSubnetId(subnetId);
        checkIpv4Addr(ipv4Addr);

        return ipv4AddrServiceImpl.releaseIpv4Addr(subnetId, ipv4Addr);
    }

    @DeleteMapping("/v4/ips/bulk")
    @ResponseBody
    public Ipv4AddrRequestBulk releaseIpv4AddrBulk(@RequestBody Ipv4AddrRequestBulk requestBulk) throws Exception {
        for(Ipv4AddrRequest request : requestBulk.getIpv4AddrRequests()) {
            checkSubnetId(request.getSubnetId());
        }

        return ipv4AddrServiceImpl.releaseIpv4AddrBulk(requestBulk);
    }

    @GetMapping("/v4/ips/{subnet_id}/{ip}")
    @ResponseBody
    public Ipv4AddrRequest getIpv4Addr(@PathVariable("subnet_id") String subnetId, @PathVariable("ip") String ipv4Addr) throws Exception {
        checkSubnetId(subnetId);
        checkIpv4Addr(ipv4Addr);

        return ipv4AddrServiceImpl.getIpv4Addr(subnetId, ipv4Addr);
    }

    @GetMapping("/v4/ips")
    @ResponseBody
    public List<Ipv4AddrRequest> listAllocatedIpv4Addr() {
        return ipv4AddrServiceImpl.listAllocatedIpv4Addr();
    }

    @PostMapping("/v4/ips/range")
    @ResponseBody
    public Ipv4AddrRangeRequest createIpv4AddrRange(@RequestBody Ipv4AddrRangeRequest request) throws Exception {
        checkSubnetId(request.getSubnetId());
        checkIpv4Addr(request.getFirstAddr());
        checkIpv4Addr(request.getLastAddr());

        //Check if first < last
        long firstAddrLong = Ipv4AddrUtil.ipToLong(request.getFirstAddr());
        long lastAddrLong = Ipv4AddrUtil.ipToLong(request.getLastAddr());
        if (firstAddrLong >= lastAddrLong) {
            throw new Ipv4AddrRangeInvalidException();
        }

        return ipv4AddrServiceImpl.createIpv4AddrRange(request);
    }

    @DeleteMapping("/v4/ips/range/{subnet_id}")
    @ResponseBody
    public Ipv4AddrRangeRequest deleteIpv4AddrRange(@PathVariable("subnet_id") String subnetId) throws Exception {
        return ipv4AddrServiceImpl.deleteIpv4AddrRange(subnetId);
    }

    @GetMapping("/v4/ips/range/{subnet_id}")
    @ResponseBody
    public Ipv4AddrRangeRequest getIpv4AddrRange(@PathVariable("subnet_id") String subnetId) throws Exception {
        checkSubnetId(subnetId);

        return ipv4AddrServiceImpl.getIpv4AddrRange(subnetId);
    }

    @GetMapping("/v4/ips/range")
    @ResponseBody
    public List<Ipv4AddrRangeRequest> listIpv4AddrRange() {
        return ipv4AddrServiceImpl.listIpv4AddrRange();
    }
}
