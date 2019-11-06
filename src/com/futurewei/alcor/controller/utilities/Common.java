package com.futurewei.alcor.controller.utilities;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Common {
    public static byte[] fromIpAddressStringToByteArray(String ipAddressString) throws UnknownHostException {
        InetAddress ip = InetAddress.getByName(ipAddressString);
        byte[] bytes = ip.getAddress();

        return bytes;
    }
}
