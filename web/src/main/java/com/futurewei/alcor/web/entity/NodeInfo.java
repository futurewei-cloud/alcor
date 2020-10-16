/*Copyright 2019 The Alcor Authors.
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
package com.futurewei.alcor.web.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class NodeInfo implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(NodeInfo.class);

    @JsonProperty("node_id")
    private String id;

    @JsonProperty("node_name")
    private String name;

    @JsonProperty("local_ip")
    private String localIp;

    @JsonProperty("mac_address")
    private String macAddress;

    @JsonProperty("veth")
    private String veth;

    @JsonProperty("server_port")
    private int gRPCServerPort;

    @JsonProperty("host_dvr_mac")
    private String host_dvr_mac;

    public NodeInfo() {

    }

    public NodeInfo(NodeInfo nodeInfo) {
        this(nodeInfo.id, nodeInfo.name, nodeInfo.localIp, nodeInfo.macAddress, nodeInfo.veth, nodeInfo.gRPCServerPort, nodeInfo.host_dvr_mac);
    }

    public NodeInfo(String id, String name, String localIp, String macAddress, String veth, int gRPCServerPort) {
        this(id, name, localIp, macAddress);
        this.veth = veth;
        this.gRPCServerPort = gRPCServerPort;
    }

    public NodeInfo(String nodeId, String nodeName, String ipAddress, String macAddress, int gRPCServerPort) {
        this(nodeId, nodeName, ipAddress, macAddress);
        this.veth = "";
        this.gRPCServerPort = gRPCServerPort;
    }

    public NodeInfo(String nodeId, String nodeName, String ipAddress, String macAddress) {
        this.id = nodeId;
        this.name = nodeName;
        if (this.validateIp(ipAddress)) {
            this.localIp = ipAddress;
        } else {
            this.localIp = "";
        }

        if (this.validateMac(macAddress)) {
            this.macAddress = macAddress;
        } else {
            this.macAddress = "";
        }
        this.veth = "";
        this.gRPCServerPort = 0;
    }

    public NodeInfo(String id, String name, String localIp, String macAddress, String veth, int gRPCServerPort, String host_dvr_mac) {
        this.id = id;
        this.name = name;
        this.localIp = localIp;
        this.macAddress = macAddress;
        this.veth = veth;
        this.gRPCServerPort = gRPCServerPort;
        this.host_dvr_mac = host_dvr_mac;
    }

    public boolean validateMac(String mac) {
        Pattern p = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
        Matcher m = p.matcher(mac);
        return m.find();
    }

    public boolean validateIp(String ip) {
        Pattern p = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
        Matcher m = p.matcher(ip);
        return m.find();
    }

    public static Logger getLogger() {
        return logger;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getVeth() {
        return veth;
    }

    public void setVeth(String veth) {
        this.veth = veth;
    }

    public int getgRPCServerPort() {
        return gRPCServerPort;
    }

    public void setgRPCServerPort(int gRPCServerPort) {
        this.gRPCServerPort = gRPCServerPort;
    }

    public String getHost_dvr_mac() {
        return host_dvr_mac;
    }

    public void setHost_dvr_mac(String host_dvr_mac) {
        this.host_dvr_mac = host_dvr_mac;
    }
}