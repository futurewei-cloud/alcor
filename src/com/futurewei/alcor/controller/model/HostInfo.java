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

package com.futurewei.alcor.controller.model;

import com.futurewei.alcor.controller.logging.Log;
import com.futurewei.alcor.controller.logging.LogFactory;
import lombok.Data;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class HostInfo {

    private String id;
    private InetAddress localIp;
    private String macAddress;
    private int gRPCServerPort;

    public HostInfo(String hostId, String hostName, byte[] ipAddress, String macAddress, int gRPCServerPort) {
        this(hostId, hostName, ipAddress, macAddress);
        this.gRPCServerPort = gRPCServerPort;
    }

    public HostInfo(String hostId, String hostName, byte[] ipAddress, String macAddress) {

        Log alcorLog = LogFactory.getLog();

        this.id = hostId;

        try {
            this.localIp = InetAddress.getByAddress(hostName, ipAddress);
            if (this.validate(macAddress)) {
                this.macAddress = macAddress;
            } else {
                this.macAddress = null;
            }
        } catch (UnknownHostException e) {
            alcorLog.log(Level.SEVERE, "Invalid ip address" + ipAddress, e);
        }
    }

    public String getHostName() {
        return this.localIp.getHostName();
    }

    public String getHostIpAddress() {
        return this.localIp.getHostAddress();
    }

    public String getHostMacAddress() {
        return this.macAddress;
    }

    private boolean validate(String mac) {
        Pattern p = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
        Matcher m = p.matcher(mac);
        return m.find();
    }
}
