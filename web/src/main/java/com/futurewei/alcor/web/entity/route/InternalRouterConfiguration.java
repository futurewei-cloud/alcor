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
package com.futurewei.alcor.web.entity.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.enumClass.MessageType;
import java.util.List;

public class InternalRouterConfiguration {
    @JsonProperty("format_version")
    private String format_version;

    @JsonProperty("revision_number")
    private String revision_number;

    @JsonProperty("request_id")
    private String request_id;

    @JsonProperty("id")
    private String id;

    @JsonProperty("message_type")
    private MessageType message_type;

    @JsonProperty("host_dvr_mac")
    private String host_dvr_mac;

    @JsonProperty("subnet_routing_tables")
    private List<InternalSubnetRoutingTable> subnet_routing_tables;

    public InternalRouterConfiguration() {

    }

    public InternalRouterConfiguration(String id,
                                       String request_id,
                                       MessageType message_type,
                                       List<InternalSubnetRoutingTable> subnet_routing_tables) {
        this.id = id;
        this.request_id = request_id;
        this.message_type = message_type;
        this.subnet_routing_tables = subnet_routing_tables;
    }

    public String getFormatVersion() {
        return format_version;
    }
    public void setFormatVersion(String format_version) {
        this.format_version = format_version;
    }

    public String getRevisionNumber() { return revision_number; }
    public void setRevisionNumber(String revision_number) { this.revision_number = revision_number; }

    public String getId() {
        return id;
    }
    public void setId(String id) { this.id = id; }

    public String getRequestId() {
        return request_id;
    }
    public void setRequestId(String request_id) {
        this.request_id = request_id;
    }

    public MessageType getMessageType() {
        return message_type;
    }
    public void setMessageType(MessageType message_type) {
        this.message_type = message_type;
    }

    public String getHostDvrMac() {
        return host_dvr_mac;
    }
    public void setHostDvrMac(String host_dvr_mac) {
        this.host_dvr_mac = host_dvr_mac;
    }

    public List<InternalSubnetRoutingTable> getSubnetRoutingTables() {
        return subnet_routing_tables;
    }
    public void setSubnetRoutingTables(List<InternalSubnetRoutingTable> subnet_routing_tables) {
        this.subnet_routing_tables = subnet_routing_tables;
    }
}
