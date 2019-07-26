package com.futurewei.alioth.controller.model;

import lombok.Data;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Data
public class HostInfo {

    private String id;
    private InetAddress localIp;

    public HostInfo(String hostId, String hostName, byte ipAddress[]) {

        this.id = hostId;

        try{
            this.localIp = InetAddress.getByAddress(hostName, ipAddress);
        }
        catch(UnknownHostException e){
            System.err.printf("Invalid ip address" + ipAddress);
        }
    }

    public String getIpAddress(){
        return this.localIp.getAddress().toString();
    }
}
