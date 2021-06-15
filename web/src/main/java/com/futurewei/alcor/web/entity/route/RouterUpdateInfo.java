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
package com.futurewei.alcor.web.entity.route;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RouterUpdateInfo {
    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("subnet_id")
    private String subnetId;

    @JsonProperty("operation_type")
    private String operationType;

    @JsonProperty("gateway_port_ids")
    private List<String> gatewayPortIds;

    @JsonProperty("internal_router_info")
    private InternalRouterInfo internalRouterInfo;

    public enum OperationType {
        ADD("add"),
        DELETE("delete");

        private String type;

        OperationType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public RouterUpdateInfo() {

    }

    public RouterUpdateInfo(String vpcId, String subnetId, String operationType, List<String> gatewayPortIds, InternalRouterInfo internalRouterInfo) {
        this.vpcId = vpcId;
        this.subnetId = subnetId;
        this.operationType = operationType;
        this.gatewayPortIds = gatewayPortIds;
        this.internalRouterInfo = internalRouterInfo;
    }

    public InternalRouterInfo getInternalRouterInfo() {
        return internalRouterInfo;
    }

    public void setInternalRouterInfo(InternalRouterInfo internalRouterInfo) {
        this.internalRouterInfo = internalRouterInfo;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public List<String> getGatewayPortIds() {
        return gatewayPortIds;
    }

    public void setGatewayPortIds(List<String> gatewayPortIds) {
        this.gatewayPortIds = gatewayPortIds;
    }
}
