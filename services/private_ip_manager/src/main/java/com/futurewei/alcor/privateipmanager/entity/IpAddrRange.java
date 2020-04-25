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

package com.futurewei.alcor.privateipmanager.entity;

import com.futurewei.alcor.privateipmanager.allocator.IpAddrAllocator;
import com.futurewei.alcor.privateipmanager.allocator.Ipv4AddrAllocator;
import com.futurewei.alcor.privateipmanager.allocator.Ipv6AddrAllocator;
import com.futurewei.alcor.privateipmanager.exception.IpAddrAllocNotFoundException;
import com.futurewei.alcor.privateipmanager.exception.IpAddrInvalidException;
import com.futurewei.alcor.privateipmanager.utils.Ipv4AddrUtil;
import com.futurewei.alcor.privateipmanager.utils.Ipv6AddrUtil;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IpAddrRange {
    private String id;
    private String subnetId;
    private int ipVersion;
    private String firstIp;
    private String lastIp;
    private long usedIps;
    private long totalIps;

    private IpAddrAllocator allocator;
    Map<String, IpAddrAlloc> allocated;

    public IpAddrRange(String id, String subnetId, int ipVersion, String firstIp, String lastIp) {
        this.id = id;
        this.subnetId = subnetId;
        this.ipVersion = ipVersion;
        this.firstIp = firstIp;
        this.lastIp = lastIp;

        if (ipVersion == IpVersion.IPV4.getVersion()) {
            long firstIpLong = Ipv4AddrUtil.ipv4ToLong(firstIp);
            long lastIpLong = Ipv4AddrUtil.ipv4ToLong(lastIp);

            totalIps = lastIpLong - firstIpLong + 1;
            allocator = new Ipv4AddrAllocator(firstIpLong, lastIpLong);
        } else {
            BigInteger firstIpBigInt = Ipv6AddrUtil.ipv6ToBitInt(firstIp);
            BigInteger lastIpBigInt = Ipv6AddrUtil.ipv6ToBitInt(lastIp);

            totalIps = lastIpBigInt.subtract(firstIpBigInt).longValue() + 1;
            allocator = new Ipv6AddrAllocator(firstIpBigInt, lastIpBigInt);
        }

        allocated = new HashMap<>();
    }

    private void updateUsedIps() {
        usedIps = allocated.size();
    }

    public String allocate() throws Exception {
        String ipAddr = allocator.allocate();
        IpAddrAlloc ipAddrAlloc = new IpAddrAlloc(ipVersion, id, ipAddr, IpAddrState.ACTIVATED.getState());

        allocated.put(ipAddr, ipAddrAlloc);
        updateUsedIps();

        return ipAddr;
    }

    public List<String> allocateBulk(int num) throws Exception {
        List<String> ipAddrList = allocator.allocateBulk(num);

        for (String ipAddr: ipAddrList) {
            IpAddrAlloc ipAddrAlloc = new IpAddrAlloc(ipVersion, id, ipAddr, IpAddrState.ACTIVATED.getState());

            allocated.put(ipAddr, ipAddrAlloc);
        }

        updateUsedIps();

        return ipAddrList;
    }

    public void modifyIpAddrState(String ipAddr, String state) throws Exception {
        IpAddrAlloc ipAddrAlloc = allocated.get(ipAddr);
        if (ipAddrAlloc == null) {
            throw new IpAddrAllocNotFoundException();
        }

        if (!ipAddrAlloc.getState().equals(state)) {
            ipAddrAlloc.setState(state);
        }
    }

    public void release(String ipAddr) throws Exception {
        if (allocated.get(ipAddr) == null) {
            throw new IpAddrAllocNotFoundException();
        }

        allocator.release(ipAddr);
        allocated.remove(ipAddr);

        updateUsedIps();
    }

    public void releaseBulk(List<String> ipAddrList) throws Exception {
        allocator.releaseBulk(ipAddrList);
        for (String ipAddr: ipAddrList) {
            if (allocated.get(ipAddr) == null) {
                throw new IpAddrAllocNotFoundException();
            }

            allocated.remove(ipAddr);
        }

        updateUsedIps();
    }

    public IpAddrAlloc getIpAddr(String ipAddr) throws Exception {
        IpAddrAlloc ipAddrAlloc = allocated.get(ipAddr);
        if (ipAddrAlloc != null) {
            return ipAddrAlloc;
        }

        if (allocator.validate(ipAddr)) {
            return new IpAddrAlloc(ipVersion, id, ipAddr, IpAddrState.FREE.getState());
        }

        throw new IpAddrInvalidException();
    }

    public Collection<IpAddrAlloc> getIpAddrBulk() {
        return allocated.values();
    }

    public int getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(int ipVersion) {
        this.ipVersion = ipVersion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getFirstIp() {
        return firstIp;
    }

    public void setFirstIp(String firstIp) {
        this.firstIp = firstIp;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public IpAddrAllocator getAllocator() {
        return allocator;
    }

    public void setAllocator(IpAddrAllocator allocator) {
        this.allocator = allocator;
    }

    public long getUsedIps() {
        return usedIps;
    }

    public void setUsedIps(int usedIps) {
        this.usedIps = usedIps;
    }

    public long getTotalIps() {
        return totalIps;
    }

    public void setTotalIps(int totalIps) {
        this.totalIps = totalIps;
    }

    @Override
    public String toString() {
        return "IpAddrRange{" +
                "id='" + id + '\'' +
                ", subnetId='" + subnetId + '\'' +
                ", ipVersion=" + ipVersion +
                ", firstIp='" + firstIp + '\'' +
                ", lastIp='" + lastIp + '\'' +
                ", usedIps=" + usedIps +
                ", totalIps=" + totalIps +
                ", allocator=" + allocator +
                ", allocated=" + allocated +
                '}';
    }
}
