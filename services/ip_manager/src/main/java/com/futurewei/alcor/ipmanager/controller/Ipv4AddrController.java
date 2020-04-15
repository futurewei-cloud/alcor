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

package com.futurewei.alcor.ipmanager.controller;
import com.futurewei.alcor.ipmanager.entity.Ipv4AddrState;
import com.futurewei.alcor.ipmanager.http.Ipv4AddrRequest;
import com.futurewei.alcor.ipmanager.http.Ipv4AddrRangeRequest;
import com.futurewei.alcor.ipmanager.http.Ipv4AddrRequestBulk;
import com.futurewei.alcor.ipmanager.http.status.Ipv4AddrInvalidException;
import com.futurewei.alcor.ipmanager.http.status.Ipv4AddrRangeInvalidException;
import com.futurewei.alcor.ipmanager.http.status.Ipv4AddrStateInvalidException;
import com.futurewei.alcor.ipmanager.http.status.SubnetIdInvalidException;
import com.futurewei.alcor.ipmanager.service.Ipv4AddrService;
import com.futurewei.alcor.utils.Ipv4AddrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
public class Ipv4AddrController {
    @Autowired
    Ipv4AddrService ipv4AddrService;

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

        return ipv4AddrService.allocateIpv4Addr(request);
    }

    @PostMapping("/v4/ips/bulk")
    @ResponseBody
    public Ipv4AddrRequestBulk allocateIpv4AddrBulk(@RequestBody Ipv4AddrRequestBulk requestBulk) throws Exception {
        for(Ipv4AddrRequest request : requestBulk.getIpv4AddrRequests()) {
            checkSubnetId(request.getSubnetId());
        }

        return ipv4AddrService.allocateIpv4AddrBulk(requestBulk);
    }

    @PutMapping("/v4/ips")
    @ResponseBody
    public Ipv4AddrRequest modifyIpv4AddrState(@RequestBody Ipv4AddrRequest request) throws Exception {
        checkSubnetId(request.getSubnetId());
        checkIpv4Addr(request.getIpv4Addr());
        checkIpv4AddrState(request.getState());

        return ipv4AddrService.modifyIpv4AddrState(request);
    }

    @PutMapping("/v4/ips/bulk")
    @ResponseBody
    public Ipv4AddrRequestBulk modifyIpv4AddrStateBulk(@RequestBody Ipv4AddrRequestBulk requestBulk) throws Exception {
        for(Ipv4AddrRequest request : requestBulk.getIpv4AddrRequests()) {
            checkSubnetId(request.getSubnetId());
            checkSubnetId(request.getIpv4Addr());
            checkSubnetId(request.getState());
        }

        return ipv4AddrService.modifyIpv4AddrStateBulk(requestBulk);
    }

    @DeleteMapping("/v4/ips/{subnet_id}/{ip}")
    @ResponseBody
    public Ipv4AddrRequest releaseIpv4Addr(@PathVariable("subnet_id") String subnetId, @PathVariable("ip") String ipv4Addr) throws Exception {
        checkSubnetId(subnetId);
        checkIpv4Addr(ipv4Addr);

        return ipv4AddrService.releaseIpv4Addr(subnetId, ipv4Addr);
    }

    @DeleteMapping("/v4/ips/bulk")
    @ResponseBody
    public Ipv4AddrRequestBulk releaseIpv4AddrBulk(@RequestBody Ipv4AddrRequestBulk requestBulk) throws Exception {
        for(Ipv4AddrRequest request : requestBulk.getIpv4AddrRequests()) {
            checkSubnetId(request.getSubnetId());
        }

        return ipv4AddrService.releaseIpv4AddrBulk(requestBulk);
    }

    @GetMapping("/v4/ips/{subnet_id}/{ip}")
    @ResponseBody
    public Ipv4AddrRequest getIpv4Addr(@PathVariable("subnet_id") String subnetId, @PathVariable("ip") String ipv4Addr) throws Exception {
        checkSubnetId(subnetId);
        checkIpv4Addr(ipv4Addr);

        return ipv4AddrService.getIpv4Addr(subnetId, ipv4Addr);
    }

    @GetMapping("/v4/ips")
    @ResponseBody
    public Map listAllocatedIpv4Addr() {
        return ipv4AddrService.listAllocatedIpv4Addr();
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

        return ipv4AddrService.createIpv4AddrRange(request);
    }

    @DeleteMapping("/v4/ips/range/{subnet_id}")
    @ResponseBody
    public Ipv4AddrRangeRequest deleteIpv4AddrRange(@PathVariable("subnet_id") String subnetId) throws Exception {
        return ipv4AddrService.deleteIpv4AddrRange(subnetId);
    }

    @GetMapping("/v4/ips/range/{subnet_id}")
    @ResponseBody
    public Ipv4AddrRangeRequest getIpv4AddrRange(@PathVariable("subnet_id") String subnetId) throws Exception {
        checkSubnetId(subnetId);

        return ipv4AddrService.getIpv4AddrRange(subnetId);
    }

    @GetMapping("/v4/ips/range")
    @ResponseBody
    public Map listIpv4AddrRange() {
        return ipv4AddrService.listIpv4AddrRange();
    }
}
