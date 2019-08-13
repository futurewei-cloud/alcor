package com.futurewei.alioth.controller.app.demo;

public interface DemoConfig {
//    String HOST_ID_PREFIX = "hostid-";
    boolean isDemo = true;

    long tunnelId = 3000;
    int OVERFLOW_IP_CONVERSION = 256;

    // TODO: figure out to store integer value over 127 in a byte
    String TRANSIT_SWTICH_1_HOST_ID = "switchhost_1";
    byte[] TRANSIT_SWITCH_1_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,15};
    String TRANSIT_SWITCH_1_MAC = "02:42:ac:11:00:0f";

    String TRANSIT_SWTICH_2_HOST_ID = "switchhost_2";
    byte[] TRANSIT_SWITCH_2_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,16};
    String TRANSIT_SWITCH_2_MAC = "02:42:ac:11:00:10";

    String TRANSIT_SWTICH_3_HOST_ID = "switchhost_3";
    byte[] TRANSIT_SWITCH_3_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,17};
    String TRANSIT_SWITCH_3_MAC = "02:42:ac:11:00:11";

    String TRANSIT_SWTICH_4_HOST_ID = "switchhost_4";
    byte[] TRANSIT_SWITCH_4_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,18};
    String TRANSIT_SWITCH_4_MAC = "02:42:ac:11:00:12";

    String TRANSIT_ROUTER_1_HOST_ID = "routerhost_1";
    byte[] TRANSIT_ROUTER_1_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,19};
    String TRANSIT_ROUTER_1_MAC = "02:42:ac:11:00:13";

    String TRANSIT_ROUTER_2_HOST_ID = "routerhost_2";
    byte[] TRANSIT_ROUTER_2_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,20};
    String TRANSIT_ROUTER_2_MAC = "02:42:ac:11:00:14";

    String VNET_NAME = "veth0";

    String EP1_ID = "ephost_1";
    byte[] EP1_HOST_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,7};
    String EP1_HOST_MAC = "02:42:ac:11:00:07";

    String EP2_ID = "ephost_2";
    byte[] EP2_HOST_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,8};
    String EP2_HOST_MAC = "02:42:ac:11:00:08";

    String EP3_ID = "ephost_3";
    byte[] EP3_HOST_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,9};
    String EP3_HOST_MAC = "02:42:ac:11:00:09";

    String EP4_ID = "ephost_4";
    byte[] EP4_HOST_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,10};
    String EP4_HOST_MAC = "02:42:ac:11:00:0a";

    String EP5_ID = "ephost_5";
    byte[] EP5_HOST_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,11};
    String EP5_HOST_MAC = "02:42:ac:11:00:0b";

    String EP6_ID = "ephost_6";
    byte[] EP6_HOST_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,12};
    String EP6_HOST_MAC = "02:42:ac:11:00:0c";

    String EP7_ID = "ephost_7";
    byte[] EP7_HOST_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,13};
    String EP7_HOST_MAC = "02:42:ac:11:00:0d";

    String EP8_ID = "ephost_8";
    byte[] EP8_HOST_IP = new byte[]{172-OVERFLOW_IP_CONVERSION,17,0,14};
    String EP8_HOST_MAC = "02:42:ac:11:00:0e";
}
