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

package com.futurewei.alcor.web.entity.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.web.entity.subnet.HostRoute;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRoutingRuleResponse {

    @JsonProperty("internalSubnetRoutingTable")
    private InternalSubnetRoutingTable internalSubnetRoutingTable;

    @JsonProperty("hostRouteToSubnet")
    private List<HostRoute> hostRouteToSubnet;

    public UpdateRoutingRuleResponse() {}

    public UpdateRoutingRuleResponse(InternalSubnetRoutingTable internalSubnetRoutingTable, List<HostRoute> hostRouteToSubnet) {
        this.internalSubnetRoutingTable = internalSubnetRoutingTable;
        this.hostRouteToSubnet = hostRouteToSubnet;
    }

    public InternalSubnetRoutingTable getInternalSubnetRoutingTable() {
        return internalSubnetRoutingTable;
    }

    public void setInternalSubnetRoutingTable(InternalSubnetRoutingTable internalSubnetRoutingTable) {
        this.internalSubnetRoutingTable = internalSubnetRoutingTable;
    }

    public List<HostRoute> getHostRouteToSubnet() {
        return hostRouteToSubnet;
    }

    public void setHostRouteToSubnet(List<HostRoute> hostRouteToSubnet) {
        this.hostRouteToSubnet = hostRouteToSubnet;
    }
}
