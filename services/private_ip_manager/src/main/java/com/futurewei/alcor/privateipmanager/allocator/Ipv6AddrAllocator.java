/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.privateipmanager.allocator;

import com.futurewei.alcor.privateipmanager.exception.IpAddrInvalidException;
import com.futurewei.alcor.privateipmanager.exception.IpAddrNotEnoughException;
import com.futurewei.alcor.privateipmanager.utils.Ipv6AddrUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Ipv6AddrAllocator implements IpAddrAllocator {
    private BitSet bitSet;
    private BigInteger firstIp;
    private BigInteger lastIp;
    private long ipAddrNum;

    public Ipv6AddrAllocator(BigInteger firstIp, BigInteger lastIp) {
        this.firstIp = firstIp;
        this.lastIp = lastIp;
        ipAddrNum = lastIp.subtract(firstIp).add(BigInteger.valueOf(1)).longValue();
        bitSet = new BitSet();
    }


    @Override
    public String allocate(String ipAddr) throws Exception {
        int freeBit;

        if (ipAddr != null) {
            BigInteger ipBigInt = Ipv6AddrUtil.ipv6ToBitInt(ipAddr);
            if (ipBigInt.compareTo(firstIp) < 0 || ipBigInt.compareTo(lastIp) > 0) {
                throw new IpAddrInvalidException();
            }

            freeBit = (int)(ipBigInt.subtract(firstIp).longValue());
        } else {
            freeBit = bitSet.nextClearBit(0);

            if (freeBit < 0 || freeBit >= ipAddrNum) {
                throw new IpAddrNotEnoughException();
            }
        }

        bitSet.set(freeBit);

        return Ipv6AddrUtil.bigIntToIpv6(firstIp.add(BigInteger.valueOf(freeBit)));
    }

    @Override
    public List<String> allocateBulk(int num) throws Exception {
        List<Integer> freeBits = new ArrayList<>();
        List<String> ipv6AddrList = new ArrayList<>();

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
            String ipv6Addr = Ipv6AddrUtil.bigIntToIpv6(firstIp.add(BigInteger.valueOf(bit)));
            ipv6AddrList.add(ipv6Addr);
        }

        return ipv6AddrList;
    }

    @Override
    public void release(String ipAddr) throws Exception {
        BigInteger ipBigInt = Ipv6AddrUtil.ipv6ToBitInt(ipAddr);
        if (ipBigInt.compareTo(firstIp) < 0 || ipBigInt.compareTo(lastIp) > 0) {
            throw new IpAddrInvalidException();
        }

        bitSet.clear((int)(ipBigInt.subtract(firstIp).longValue()));
    }

    @Override
    public void releaseBulk(List<String> ipAddrList) throws Exception {
        for (String ipAddr: ipAddrList) {
            release(ipAddr);
        }
    }

    @Override
    public boolean validate(String ipAddr) {
        BigInteger ipBigInt = Ipv6AddrUtil.ipv6ToBitInt(ipAddr);

        return ipBigInt.compareTo(firstIp) >= 0 && ipBigInt.compareTo(lastIp) <= 0;
    }

    public BitSet getBitSet() {
        return bitSet;
    }

    public void setBitSet(BitSet bitSet) {
        this.bitSet = bitSet;
    }

    public BigInteger getFirstIp() {
        return firstIp;
    }

    public void setFirstIp(BigInteger firstIp) {
        this.firstIp = firstIp;
    }

    public BigInteger getLastIp() {
        return lastIp;
    }

    public void setLastIp(BigInteger lastIp) {
        this.lastIp = lastIp;
    }

    public long getIpAddrNum() {
        return ipAddrNum;
    }

    public void setIpAddrNum(long ipAddrNum) {
        this.ipAddrNum = ipAddrNum;
    }
}
