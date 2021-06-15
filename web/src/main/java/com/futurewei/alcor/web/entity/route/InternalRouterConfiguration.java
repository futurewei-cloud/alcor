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
