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
import com.futurewei.alcor.common.enumClass.VpcRouteTarget;

public class InternalRoutingRuleExtraInfo {
    @JsonProperty("next_hop_mac")
    private String next_hop_mac;

    @JsonProperty("destination_type")
    private VpcRouteTarget destination_type;

    public InternalRoutingRuleExtraInfo() {

    }

    public InternalRoutingRuleExtraInfo(String next_hop_mac, VpcRouteTarget destination_type) {
        this.next_hop_mac = next_hop_mac;
        this.destination_type = destination_type;
    }

    public String getNextHopMac() { return this.next_hop_mac; }
    public void setNextHopMac(String next_hop_mac) { this.next_hop_mac = next_hop_mac; }

    public VpcRouteTarget getDestinationType() { return this.destination_type; }
    public void setDestinationType(VpcRouteTarget destination_type) {
        this.destination_type = destination_type;
    }
}
