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

package com.futurewei.alcor.dataplane.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SubnetState extends CustomerResource {

    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("cidr")
    private String cidr;

    @JsonProperty("availability_zone")
    private String availabilityZone;

    @JsonProperty("gateway_ip")
    private String gatewayIp;

    @JsonProperty("dhcp_enable")
    private Boolean dhcpEnable;

    @JsonProperty("primary_dns")
    private String primaryDns;

    @JsonProperty("secondary_dns")
    private String secondaryDns;

    @JsonProperty("dns_list")
    private List<String> dnsList;

    public SubnetState() {
    }

    public SubnetState(String projectId, String vpcId, String id, String name, String cidr) {
        this(projectId, vpcId, id, name, cidr, null, null, null, false, null, null, null);
    }

    public SubnetState(String projectId, String vpcId, String id, String name, String cidr, String gatewayIp) {
        this(projectId, vpcId, id, name, cidr, null, null, gatewayIp, false, null, null, null);
    }

    public SubnetState(SubnetState state) {
        this(state.getProjectId(), state.getVpcId(), state.getId(), state.getName(), state.getCidr(), state.getDescription(),
                state.getAvailabilityZone(), state.getGatewayIp(), state.getDhcpEnable(), state.getPrimaryDns(), state.getSecondaryDns(), state.getDnsList());
    }

    public SubnetState(String projectId, String vpcId, String id, String name, String cidr, String description, String availabilityZone,
                       String gatewayIp, Boolean dhcpEnable, String primaryDns, String secondaryDns, List<String> dnsList) {

        super(projectId, id, name, description);

        this.vpcId = vpcId;
        this.cidr = cidr;
        this.availabilityZone = availabilityZone;
        this.gatewayIp = gatewayIp;
        this.dhcpEnable = dhcpEnable;
        this.primaryDns = primaryDns;
        this.secondaryDns = secondaryDns;
        this.dnsList = (dnsList == null ? null : new ArrayList<>(dnsList));
    }

}
