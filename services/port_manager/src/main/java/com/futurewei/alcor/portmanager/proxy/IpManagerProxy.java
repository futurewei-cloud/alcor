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
package com.futurewei.alcor.portmanager.proxy;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.common.utils.Ipv4AddrUtil;
import com.futurewei.alcor.common.utils.Ipv6AddrUtil;
import com.futurewei.alcor.portmanager.exception.RangeIdNotFoundException;
import com.futurewei.alcor.portmanager.exception.VerifySubnetException;
import com.futurewei.alcor.portmanager.rollback.AbstractIpAddrRollback;
import com.futurewei.alcor.web.entity.ip.IpAddrRequest;
import com.futurewei.alcor.web.entity.ip.IpVersion;
import com.futurewei.alcor.web.entity.port.PortState;
import com.futurewei.alcor.web.entity.subnet.SubnetStateJson;
import com.futurewei.alcor.web.restclient.IpManagerRestClient;
import com.futurewei.alcor.portmanager.exception.IpAddrInvalidException;
import com.futurewei.alcor.portmanager.exception.IpVersionInvalidException;
import com.futurewei.alcor.portmanager.rollback.AllocateIpAddrRollback;
import com.futurewei.alcor.portmanager.rollback.PortStateRollback;
import com.futurewei.alcor.portmanager.rollback.ReleaseIpAddrRollback;
import com.futurewei.alcor.web.restclient.SubnetManagerRestClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class IpManagerProxy {
    private IpManagerRestClient ipManagerRestClient;
    private SubnetManagerRestClient subnetManagerRestClient;
    private Stack<PortStateRollback> rollbacks;
    private String projectId;

    public IpManagerProxy(Stack<PortStateRollback> rollbacks, String projectId) {
        ipManagerRestClient = SpringContextUtil.getBean(IpManagerRestClient.class);
        subnetManagerRestClient = SpringContextUtil.getBean(SubnetManagerRestClient.class);
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
        SubnetStateJson subnetStateJson = subnetManagerRestClient.getSubnetState(projectId, subnetId);
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

    /**
     * Allocate a random ipv4 and ipv6 address from ip manager service
     * @param args PortState
     * @return A list of IpAddrRequest
     * @throws Exception Rest request exception
     */
    public List<IpAddrRequest> allocateRandomIpAddress(Object args) throws Exception {
        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        List<PortState.FixedIp> fixedIps = new ArrayList<>();
        PortState portState = (PortState)args;

        //Allocate a random ipv4 address
        IpAddrRequest ipv4Addr = ipManagerRestClient.allocateIpAddress(IpVersion.IPV4,
                portState.getVpcId(), null, null);
        PortState.FixedIp fixedIpv4 = new PortState.FixedIp(ipv4Addr.getSubnetId(), ipv4Addr.getIp());
        fixedIps.add(fixedIpv4);
        addIpAddrRollback(new AllocateIpAddrRollback(ipManagerRestClient), ipv4Addr);

        //Allocate a random ipv6 address
        IpAddrRequest ipv6Addr = ipManagerRestClient.allocateIpAddress(IpVersion.IPV6,
                portState.getVpcId(), null, null);
        PortState.FixedIp fixedIpv6 = new PortState.FixedIp(ipv6Addr.getSubnetId(), ipv6Addr.getIp());
        fixedIps.add(fixedIpv6);
        addIpAddrRollback(new AllocateIpAddrRollback(ipManagerRestClient), ipv6Addr);

        //Set fixedIps to portState
        portState.setFixedIps(fixedIps);

        ipAddrRequests.add(ipv4Addr);
        ipAddrRequests.add(ipv6Addr);

        return ipAddrRequests;
    }

    /**
     * Allocate multiple fixed ipv4/ipv6 addresses from ip manager service
     * @param args A list of IpAddrRequest
     * @return A list of IpAddrRequest
     * @throws Exception Rest request exception
     */
    public List<IpAddrRequest> allocateFixedIpAddress(Object args) throws Exception {
        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        List<PortState.FixedIp> fixedIps = (List<PortState.FixedIp>)args;

        for (PortState.FixedIp fixedIp: fixedIps) {
            int ipVersion = getIpVersion(fixedIp.getIpAddress());
            String rangeId = getRangeIdBySubnetId(fixedIp.getSubnetId(), ipVersion);
            if (rangeId == null) {
                throw new RangeIdNotFoundException();
            }

            IpAddrRequest result = ipManagerRestClient.allocateIpAddress(null,
                    null, rangeId, fixedIp.getIpAddress());

            addIpAddrRollback(new AllocateIpAddrRollback(ipManagerRestClient), result);

            ipAddrRequests.add(result);
        }

        return ipAddrRequests;
    }

    /**
     * Release multiple ipv4/ipv6 addresses to ip manager service
     * @param args A list of IpAddrRequest
     * @return A list of IpAddrRequest
     * @throws Exception Rest request exception
     */
    public List<IpAddrRequest> releaseIpAddressBulk(Object args) throws Exception {
        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        List<PortState.FixedIp> fixedIps = (List<PortState.FixedIp>)args;

        for (PortState.FixedIp fixedIp: fixedIps) {
            ipManagerRestClient.releaseIpAddress(fixedIp.getSubnetId(), fixedIp.getIpAddress());

            IpAddrRequest ipAddrRequest = new IpAddrRequest();

            int ipVersion = getIpVersion(fixedIp.getIpAddress());
            String rangeId = getRangeIdBySubnetId(fixedIp.getSubnetId(), ipVersion);
            if (rangeId == null) {
                throw new RangeIdNotFoundException();
            }

            ipAddrRequest.setRangeId(rangeId);
            ipAddrRequest.setIp(fixedIp.getIpAddress());

            addIpAddrRollback(new ReleaseIpAddrRollback(ipManagerRestClient), ipAddrRequest);

            ipAddrRequests.add(ipAddrRequest);
        }

        return ipAddrRequests;
    }
}
