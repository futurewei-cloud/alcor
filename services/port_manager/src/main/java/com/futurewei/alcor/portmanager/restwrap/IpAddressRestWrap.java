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
package com.futurewei.alcor.portmanager.restwrap;

import com.futurewei.alcor.common.utils.Ipv4AddrUtil;
import com.futurewei.alcor.common.utils.Ipv6AddrUtil;
import com.futurewei.alcor.portmanager.exception.RangeIdNotFoundException;
import com.futurewei.alcor.portmanager.exception.VerifySubnetException;
import com.futurewei.alcor.portmanager.rollback.AbstractIpAddrRollback;
import com.futurewei.alcor.portmanager.utils.BeanUtil;
import com.futurewei.alcor.web.entity.IpAddrRequest;
import com.futurewei.alcor.web.entity.IpVersion;
import com.futurewei.alcor.web.entity.PortState;
import com.futurewei.alcor.web.entity.SubnetStateJson;
import com.futurewei.alcor.web.rest.IpAddressRest;
import com.futurewei.alcor.portmanager.exception.IpAddrInvalidException;
import com.futurewei.alcor.portmanager.exception.IpVersionInvalidException;
import com.futurewei.alcor.portmanager.rollback.AllocateIpAddrRollback;
import com.futurewei.alcor.portmanager.rollback.PortStateRollback;
import com.futurewei.alcor.portmanager.rollback.ReleaseIpAddrRollback;
import com.futurewei.alcor.web.rest.SubnetRest;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class IpAddressRestWrap {
    private IpAddressRest ipAddressRest;
    private SubnetRest subnetRest;
    private Stack<PortStateRollback> rollbacks;
    private String projectId;

    public IpAddressRestWrap(Stack<PortStateRollback> rollbacks, String projectId) {
        ipAddressRest = BeanUtil.getBean(IpAddressRest.class);
        subnetRest = BeanUtil.getBean(SubnetRest.class);
        this.rollbacks = rollbacks;
        this.projectId = projectId;
    }

    private int getIpVersion(String ipAddress) throws Exception {
        if (Ipv4AddrUtil.formatCheck(ipAddress)) {
            return IpVersion.IPV4.getVersion();
        } else if (Ipv6AddrUtil.formatCheck(ipAddress)) {
            return IpVersion.IPV6.getVersion();
        } else {
            throw new IpAddrInvalidException();
        }
    }

    private String getRangeIdBySubnetId(String subnetId, int ipVersion) throws Exception {
        SubnetStateJson subnetStateJson = subnetRest.getSubnetState(projectId, subnetId);
        if (subnetStateJson == null || subnetStateJson.getSubnet() == null) {
            throw new VerifySubnetException();
        }

        if (IpVersion.IPV4.getVersion() == ipVersion) {
            return subnetStateJson.getSubnet().getIpV4RangeId();
        } else if (IpVersion.IPV6.getVersion() == ipVersion) {
            return subnetStateJson.getSubnet().getIpV4RangeId();
        }

        throw new IpVersionInvalidException();
    }

    private void addIpAddrRollback(AbstractIpAddrRollback rollback, IpAddrRequest ipAddr) {
        if (rollback instanceof AllocateIpAddrRollback) {
            rollback.putAllocatedIpAddress(ipAddr);
        } else {
            rollback.putReleasedIpAddress(ipAddr);
        }

        rollbacks.push(rollback);
    }

    public List<IpAddrRequest> allocateIpAddress(Object args) throws Exception {
        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        PortState portState = (PortState)args;

        IpAddrRequest result = ipAddressRest.allocateIpAddress(IpVersion.IPV4,
                portState.getVpcId(), null, null);

        List<PortState.FixedIp> fixedIps = new ArrayList<>();
        PortState.FixedIp fixedIp = new PortState.FixedIp(result.getSubnetId(), result.getIp());

        fixedIps.add(fixedIp);
        portState.setFixedIps(fixedIps);

        addIpAddrRollback(new AllocateIpAddrRollback(ipAddressRest), result);

        ipAddrRequests.add(result);

        return ipAddrRequests;
    }

    public List<IpAddrRequest> verifyIpAddresses(Object args) throws Exception {
        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        List<PortState.FixedIp> fixedIps = (List<PortState.FixedIp>)args;

        for (PortState.FixedIp fixedIp: fixedIps) {
            int ipVersion = getIpVersion(fixedIp.getIpAddress());
            String rangeId = getRangeIdBySubnetId(fixedIp.getSubnetId(), ipVersion);
            if (rangeId == null) {
                throw new RangeIdNotFoundException();
            }

            IpAddrRequest result = ipAddressRest.allocateIpAddress(null,
                    null, rangeId, fixedIp.getIpAddress());

            addIpAddrRollback(new AllocateIpAddrRollback(ipAddressRest), result);

            ipAddrRequests.add(result);
        }

        return ipAddrRequests;
    }

    public List<IpAddrRequest> releaseIpAddressBulk(Object args) throws Exception {
        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        List<PortState.FixedIp> fixedIps = (List<PortState.FixedIp>)args;

        for (PortState.FixedIp fixedIp: fixedIps) {
            ipAddressRest.releaseIpAddress(fixedIp.getSubnetId(), fixedIp.getIpAddress());

            IpAddrRequest ipAddrRequest = new IpAddrRequest();

            int ipVersion = getIpVersion(fixedIp.getIpAddress());
            String rangeId = getRangeIdBySubnetId(fixedIp.getSubnetId(), ipVersion);
            if (rangeId == null) {
                throw new RangeIdNotFoundException();
            }

            ipAddrRequest.setRangeId(rangeId);
            ipAddrRequest.setIp(fixedIp.getIpAddress());

            addIpAddrRollback(new ReleaseIpAddrRollback(ipAddressRest), ipAddrRequest);

            ipAddrRequests.add(ipAddrRequest);
        }

        return ipAddrRequests;
    }
}
