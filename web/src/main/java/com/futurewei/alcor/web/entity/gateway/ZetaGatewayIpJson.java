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
package com.futurewei.alcor.web.entity.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ZetaGatewayIpJson {

    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("vni")
    private String vni;

    @JsonProperty("zgc_id")
    private String zgcId;

    @JsonProperty("id")
    private String id;

    @JsonProperty("port_ibo")
    private String portIbo;

    @JsonProperty("gws")
    private List<GatewayIp> gatewayIps;

    @JsonProperty("ports")
    private List<String> ports;

    public ZetaGatewayIpJson() { }

    public ZetaGatewayIpJson(List<GatewayIp> gws, String id, String portIbo, List<String> ports,
                             String vni, String vpcId, String zgcId) {
        this.gatewayIps = gws;
        this.id = id;
        this.portIbo = portIbo;
        this.ports = ports;
        this.vni = vni;
        this.vpcId = vpcId;
        this.zgcId = zgcId;
    }
}
