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

package com.futurewei.alcor.controller.model;

import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
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

        Logger logger = LoggerFactory.getLogger();

        this.id = hostId;

        try {
            this.localIp = InetAddress.getByAddress(hostName, ipAddress);
            if (this.validate(macAddress)) {
                this.macAddress = macAddress;
            } else {
                this.macAddress = null;
            }
        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Invalid ip address" + ipAddress, e);
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
