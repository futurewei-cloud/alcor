package com.futurewei.alioth.controller.app;

public interface DemoConfig {
    String HOST_ID_PREFIX = "i2-";
    int OVERFLOW_IP_CONVERSION = 256;

    // TODO: figure out to store integer value over 127 in a byte
    String TRANSIT_SWTICH_1_HOST_ID = "switchhost_0";
    byte[] TRANSIT_SWITCH_1_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,15};
    String TRANSIT_SWITCH_1_MAC = "02:42:ac:11:00:0f";

    String TRANSIT_SWTICH_2_HOST_ID = "switchhost_2";
    byte[] TRANSIT_SWITCH_2_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,17};
    String TRANSIT_SWITCH_2_MAC = "02:42:ac:11:00:11";

    String TRANSIT_ROUTER_1_HOST_ID = "routerhost_0";
    byte[] TRANSIT_ROUTER_1_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,19};
    String TRANSIT_ROUTER_1_MAC = "02:42:ac:11:00:13";

    String TRANSIT_ROUTER_2_HOST_ID = "routerhost_1";
    byte[] TRANSIT_ROUTER_2_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,20};
    String TRANSIT_ROUTER_2_MAC = "02:42:ac:11:00:14";

    String VNET_NAME = "veth0";

    String EP1_ID = "ephost_0";
    byte[] EP1_HOST_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,7};
    String EP1_HOST_MAC = "02:42:ac:11:00:07";

    String EP2_ID = "ephost_1";
    byte[] EP2_HOST_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,8};
    String EP2_HOST_MAC = "02:42:ac:11:00:08";

    String EP3_ID = "ephost_4";
    byte[] EP3_HOST_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,11};
    String EP3_HOST_MAC = "02:42:ac:11:00:0b";

    String EP4_ID = "ephost_5";
    byte[] EP4_HOST_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,12};
    String EP4_HOST_MAC = "02:42:ac:11:00:0c";
}
