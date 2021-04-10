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

package com.futurewei.alcor.portmanager.rollback;


import com.futurewei.alcor.web.entity.ip.IpAddrRequest;
import com.futurewei.alcor.web.restclient.IpManagerRestClient;
import java.util.ArrayList;
import java.util.List;


public abstract class AbstractIpAddrRollback implements Rollback {
    protected IpManagerRestClient ipManagerRestClient;

    protected List<IpAddrRequest> allocatedIps = new ArrayList<>();
    protected List<IpAddrRequest> releasedIps = new ArrayList<>();

    public AbstractIpAddrRollback(IpManagerRestClient ipManagerRestClient) {
        this.ipManagerRestClient = ipManagerRestClient;
    }

    public abstract void doRollback() throws Exception;

    public void putAllocatedIpAddress(IpAddrRequest ipAddrRequest) {
        allocatedIps.add(ipAddrRequest);
    }

    public void putReleasedIpAddress(IpAddrRequest ipAddrRequest) {
        releasedIps.add(ipAddrRequest);
    }

    public void putAllocatedIpAddresses(List<IpAddrRequest> ipAddrRequests) {
        allocatedIps.addAll(ipAddrRequests);
    }

    public void putReleasedIpAddresses(List<IpAddrRequest> ipAddrRequests) {
        releasedIps.addAll(ipAddrRequests);
    }
}
