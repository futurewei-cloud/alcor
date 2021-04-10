/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.portmanager.proxy;

import com.futurewei.alcor.common.utils.Ipv4AddrUtil;
import com.futurewei.alcor.common.utils.Ipv6AddrUtil;
import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.exception.RangeIdNotFoundException;
import com.futurewei.alcor.portmanager.exception.VerifySubnetException;
import com.futurewei.alcor.portmanager.rollback.AbstractIpAddrRollback;
import com.futurewei.alcor.web.entity.ip.IpAddrRequest;
import com.futurewei.alcor.web.entity.ip.IpVersion;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.restclient.IpManagerRestClient;
import com.futurewei.alcor.portmanager.exception.IpAddrInvalidException;
import com.futurewei.alcor.portmanager.exception.IpVersionInvalidException;
import com.futurewei.alcor.portmanager.rollback.AllocateIpAddrRollback;
import com.futurewei.alcor.portmanager.rollback.Rollback;
import com.futurewei.alcor.portmanager.rollback.ReleaseIpAddrRollback;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.restclient.RouteManagerRestClient;
import com.futurewei.alcor.web.restclient.SubnetManagerRestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class IpManagerProxy {
    private IpManagerRestClient ipManagerRestClient;
    private SubnetManagerRestClient subnetManagerRestClient;
    private RouteManagerRestClient routeManagerRestClient;
    private Stack<Rollback> rollbacks;
    private String projectId;

    public IpManagerProxy(Stack<Rollback> rollbacks, String projectId) {
        ipManagerRestClient = SpringContextUtil.getBean(IpManagerRestClient.class);
        subnetManagerRestClient = SpringContextUtil.getBean(SubnetManagerRestClient.class);
        routeManagerRestClient = SpringContextUtil.getBean(RouteManagerRestClient.class);
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
        SubnetWebJson subnetStateJson = subnetManagerRestClient.getSubnet(projectId, subnetId);
        if (subnetStateJson == null || subnetStateJson.getSubnet() == null) {
            throw new VerifySubnetException();
        }

        if (IpVersion.IPV4.getVersion() == ipVersion) {
            return subnetStateJson.getSubnet().getIpV4RangeId();
        } else if (IpVersion.IPV6.getVersion() == ipVersion) {
            return subnetStateJson.getSubnet().getIpV6RangeId();
        }

        throw new IpVersionInvalidException();
    }

    private String getRangeIdFromSubnetEntity(SubnetEntity subnetEntity, int ipVersion) throws Exception {
        if (IpVersion.IPV4.getVersion() == ipVersion) {
            return subnetEntity.getIpV4RangeId();
        } else if (IpVersion.IPV6.getVersion() == ipVersion) {
            return subnetEntity.getIpV6RangeId();
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
    public PortEntity.FixedIp allocateRandomIpAddress(Object args) throws Exception {
        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        List<PortEntity.FixedIp> fixedIps = new ArrayList<>();
        PortEntity portEntity = (PortEntity)args;

        //Allocate a random ipv4 address
        IpAddrRequest ipv4Addr = ipManagerRestClient.allocateIpAddress(IpVersion.IPV4,
                portEntity.getVpcId(), null, null);
        PortEntity.FixedIp fixedIpv4 = new PortEntity.FixedIp(ipv4Addr.getSubnetId(), ipv4Addr.getIp());
        fixedIps.add(fixedIpv4);
        addIpAddrRollback(new AllocateIpAddrRollback(ipManagerRestClient), ipv4Addr);

        //Allocate a random ipv6 address
        /*
        IpAddrRequest ipv6Addr = ipManagerRestClient.allocateIpAddress(IpVersion.IPV6,
                portEntity.getNetworkId(), null, null);
        PortEntity.FixedIp fixedIpv6 = new PortEntity.FixedIp(ipv6Addr.getSubnetId(), ipv6Addr.getIp());

        fixedIps.add(fixedIpv6);
        addIpAddrRollback(new AllocateIpAddrRollback(ipManagerRestClient), ipv6Addr);
        */
        //Set fixedIps to portState
        portEntity.setFixedIps(fixedIps);

        ipAddrRequests.add(ipv4Addr);
        //ipAddrRequests.add(ipv6Addr);

        return fixedIpv4;
    }

    public IpAddrRequest allocateFixedIpAddress(Object arg1, Object arg2) throws Exception {
        SubnetEntity subnetEntity = (SubnetEntity)arg1;
        PortEntity.FixedIp fixedIp = (PortEntity.FixedIp)arg2;

        String rangeId = getRangeIdFromSubnetEntity(subnetEntity, getIpVersion(fixedIp.getIpAddress()));
        if (rangeId == null) {
            throw new RangeIdNotFoundException();
        }

        IpAddrRequest result = ipManagerRestClient.allocateIpAddress(null,
                null, rangeId, fixedIp.getIpAddress());
        addIpAddrRollback(new AllocateIpAddrRollback(ipManagerRestClient), result);

        return result;
    }

    /**
     * Allocate multiple fixed ipv4/ipv6 addresses from ip manager service
     * @param args A list of IpAddrRequest
     * @return A list of IpAddrRequest
     * @throws Exception Rest request exception
     */
    public List<IpAddrRequest> allocateFixedIpAddresses(Object args) throws Exception {
        List<IpAddrRequest> ipAddrRequests = new ArrayList<>();
        List<PortEntity.FixedIp> fixedIps = (List<PortEntity.FixedIp>)args;

        for (PortEntity.FixedIp fixedIp: fixedIps) {
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
        List<PortEntity.FixedIp> fixedIps = (List<PortEntity.FixedIp>)args;

        for (PortEntity.FixedIp fixedIp: fixedIps) {
            int ipVersion = getIpVersion(fixedIp.getIpAddress());
            String rangeId = getRangeIdBySubnetId(fixedIp.getSubnetId(), ipVersion);
            if (rangeId == null) {
                throw new RangeIdNotFoundException();
            }

            ipManagerRestClient.releaseIpAddress(rangeId, fixedIp.getIpAddress());

            IpAddrRequest ipAddrRequest = new IpAddrRequest();
            ipAddrRequest.setRangeId(rangeId);
            ipAddrRequest.setIp(fixedIp.getIpAddress());

            addIpAddrRollback(new ReleaseIpAddrRollback(ipManagerRestClient), ipAddrRequest);

            ipAddrRequests.add(ipAddrRequest);
        }

        return ipAddrRequests;
    }
}
