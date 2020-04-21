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
    private int ipVersion;
    private String firstAddr;
    private String lastAddr;

    private IpAddrAllocator allocator;
    Map<String, IpAddrAlloc> allocated;

    public IpAddrRange(String id, int ipVersion, String firstAddr, String lastAddr) {
        this.id = id;
        this.ipVersion = ipVersion;
        this.firstAddr = firstAddr;
        this.lastAddr = lastAddr;

        if (ipVersion == IpVersion.IPV4.getVersion()) {
            long firstIpLong = Ipv4AddrUtil.ipv4ToLong(firstAddr);
            long lastIpLong = Ipv4AddrUtil.ipv4ToLong(lastAddr);

            allocator = new Ipv4AddrAllocator(firstIpLong, lastIpLong);
        } else {
            BigInteger firstIpLong = Ipv6AddrUtil.ipv6ToBitInt(firstAddr);
            BigInteger lastIpLong = Ipv6AddrUtil.ipv6ToBitInt(lastAddr);

            allocator = new Ipv6AddrAllocator(firstIpLong, lastIpLong);
        }

        allocated = new HashMap<>();
    }

    public String allocate() throws Exception {
        String ipAddr = allocator.allocate();
        IpAddrAlloc ipAddrAlloc = new IpAddrAlloc(ipVersion, id, ipAddr, IpAddrState.ACTIVATED.getState());

        allocated.put(ipAddr, ipAddrAlloc);

        return ipAddr;
    }

    public List<String> allocateBulk(int num) throws Exception {
        List<String> ipAddrList = allocator.allocateBulk(num);

        for (String ipAddr: ipAddrList) {
            IpAddrAlloc ipAddrAlloc = new IpAddrAlloc(ipVersion, id, ipAddr, IpAddrState.ACTIVATED.getState());

            allocated.put(ipAddr, ipAddrAlloc);
        }

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
        allocator.release(ipAddr);
        allocated.remove(ipAddr);
    }

    public void releaseBulk(List<String> ipAddrList) throws Exception {
        allocator.releaseBulk(ipAddrList);
        for (String ipAddr: ipAddrList) {
            allocated.remove(ipAddr);
        }
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

    public String getFirstAddr() {
        return firstAddr;
    }

    public void setFirstAddr(String firstAddr) {
        this.firstAddr = firstAddr;
    }

    public String getLastAddr() {
        return lastAddr;
    }

    public void setLastAddr(String lastAddr) {
        this.lastAddr = lastAddr;
    }

    public IpAddrAllocator getAllocator() {
        return allocator;
    }

    public void setAllocator(IpAddrAllocator allocator) {
        this.allocator = allocator;
    }
}
