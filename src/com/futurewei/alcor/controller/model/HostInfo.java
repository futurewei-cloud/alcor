package com.futurewei.alcor.controller.model;

import lombok.Data;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

        this.id = hostId;

        try{
            this.localIp = InetAddress.getByAddress(hostName, ipAddress);
            if(this.validate(macAddress)) {
                this.macAddress = macAddress;
            }else{
                this.macAddress = null;
            }
        }
        catch(UnknownHostException e){
            System.err.printf("Invalid ip address" + ipAddress);
        }
    }

    public String getHostName(){
        return this.localIp.getHostName();
    }

    public String getHostIpAddress(){
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
