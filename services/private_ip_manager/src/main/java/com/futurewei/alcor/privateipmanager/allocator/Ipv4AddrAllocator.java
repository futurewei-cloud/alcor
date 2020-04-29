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
package com.futurewei.alcor.privateipmanager.allocator;

import com.futurewei.alcor.privateipmanager.exception.IpAddrInvalidException;
import com.futurewei.alcor.privateipmanager.exception.IpAddrNotEnoughException;
import com.futurewei.alcor.privateipmanager.utils.Ipv4AddrUtil;

import java.util.*;

public class Ipv4AddrAllocator implements IpAddrAllocator {
    private BitSet bitSet;
    private long firstIp;
    private long lastIp;
    private long ipAddrNum;

    public Ipv4AddrAllocator(long firstIp, long lastIp) {
        this.firstIp = firstIp;
        this.lastIp = lastIp;
        ipAddrNum = lastIp - firstIp + 1;
        bitSet = new BitSet();
    }

    @Override
    public String allocate(String ipAddr) throws Exception {
        int freeBit;

        if (ipAddr != null) {
            long ipLong = Ipv4AddrUtil.ipv4ToLong(ipAddr);
            if (ipLong < firstIp || ipLong > lastIp) {
                throw new IpAddrInvalidException();
            }

            freeBit = (int)(ipLong - firstIp);
        } else {
            freeBit = bitSet.nextClearBit(0);

            if (freeBit < 0 || freeBit >= ipAddrNum) {
                throw new IpAddrNotEnoughException();
            }
        }

        bitSet.set(freeBit);

        return Ipv4AddrUtil.longToIpv4(firstIp + freeBit);
    }

    @Override
    public List<String> allocateBulk(int num) throws Exception {
        List<Integer> freeBits = new ArrayList<>();
        List<String> ipv4AddrList = new ArrayList<>();

        int freeBit = 0;
        while (freeBits.size() < num) {
            freeBit = bitSet.nextClearBit(freeBit + 1);
            if (freeBit < 0 || freeBit >= ipAddrNum) {
                throw new IpAddrNotEnoughException();
            }

            freeBits.add(freeBit);
        }

        for (int bit: freeBits) {
            bitSet.set(bit);
            String ipv4Addr = Ipv4AddrUtil.longToIpv4(firstIp + bit);
            ipv4AddrList.add(ipv4Addr);
        }

        return ipv4AddrList;
    }

    public void release(String ipAddr) throws Exception {
        long ipLong = Ipv4AddrUtil.ipv4ToLong(ipAddr);
        if (ipLong < firstIp || ipLong > lastIp) {
            throw new IpAddrInvalidException();
        }

        bitSet.clear((int)(ipLong - firstIp));
    }

    @Override
    public void releaseBulk(List<String> ipAddrList) throws Exception {
        for (String ipAddr: ipAddrList) {
            release(ipAddr);
        }
    }

    @Override
    public boolean validate(String ipAddr) {
        long ipLong = Ipv4AddrUtil.ipv4ToLong(ipAddr);

        return ipLong >= firstIp && ipLong <= lastIp;
    }
}
