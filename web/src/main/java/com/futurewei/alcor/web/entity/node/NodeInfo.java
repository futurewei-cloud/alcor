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
package com.futurewei.alcor.web.entity.node;

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
    private String hostDvrMac;

    @JsonProperty("unicast_topic")
    private String unicastTopic;

    @JsonProperty("multicast_topic")
    private String multicastTopic;

    @JsonProperty("group_topic")
    private String groupTopic;

    @JsonProperty("ncm_id")
    private String ncm_id;

    // doesn't come in the Json version
    private String ncm_uri;

    public NodeInfo() {

    }

    public NodeInfo(NodeInfo nodeInfo) {
        this(nodeInfo.id, nodeInfo.name, nodeInfo.localIp, nodeInfo.macAddress, nodeInfo.veth, nodeInfo.gRPCServerPort, nodeInfo.hostDvrMac, nodeInfo.unicastTopic, nodeInfo.multicastTopic, nodeInfo.groupTopic);
        this.ncm_id = nodeInfo.ncm_id;
    }

    public NodeInfo(String id, String name, String localIp, String macAddress, String veth, int gRPCServerPort, String unicastTopic, String multicastTopic, String groupTopic) {
        this(id, name, localIp, macAddress, unicastTopic, multicastTopic, groupTopic);
        this.veth = veth;
        this.gRPCServerPort = gRPCServerPort;
    }

    public NodeInfo(String nodeId, String nodeName, String ipAddress, String macAddress, int gRPCServerPort, String unicastTopic, String multicastTopic, String groupTopic) {
        this(nodeId, nodeName, ipAddress, macAddress, unicastTopic, multicastTopic, groupTopic);
        this.veth = "";
        this.gRPCServerPort = gRPCServerPort;
    }

    public NodeInfo(String nodeId, String nodeName, String ipAddress, String macAddress, String unicastTopic, String multicastTopic, String groupTopic) {
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
        this.unicastTopic = unicastTopic;
        this.multicastTopic = multicastTopic;
        this.groupTopic = groupTopic;
    }

    public NodeInfo(String id, String name, String localIp, String macAddress, String veth, int gRPCServerPort, String host_dvr_mac, String unicastTopic, String multicastTopic, String groupTopic) {
        this(id, name, localIp, macAddress, veth, gRPCServerPort, unicastTopic, multicastTopic, groupTopic);
        this.hostDvrMac = host_dvr_mac;
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

    public String getHostDvrMac() {
        return hostDvrMac;
    }

    public void setHostDvrMac(String hostDvrMac) {
        this.hostDvrMac = hostDvrMac;
    }

    public String getUnicastTopic() { return unicastTopic; }

    public void setUnicastTopic(String unicastTopic) { this.unicastTopic = unicastTopic; }

    public String getMulticastTopic() { return multicastTopic; }

    public void setMulticastTopic(String multicastTopic) { this.multicastTopic = multicastTopic; }

    public String getGroupTopic() { return groupTopic; }

    public void setGroupTopic(String groupTopic) { this.groupTopic = groupTopic;  }

    public String getNcmId() { return ncm_id; }

    public void setNcmId(String ncm_id) { this.ncm_id = ncm_id; }

    public String getNcmUri() { return ncm_uri; }

    public void setNcmUri(String uri) { ncm_uri = uri; }
}