package com.futurewei.alioth.controller.app;

public interface DemoConfig {
    String HOST_ID_PREFIX = "hostid-";

    // TODO: figure out to store integer value over 127 in a byte
    String TRANSIT_SWTICH_1_HOST_ID = "ts-1";
    byte[] TRANSIT_SWITCH_1_IP = new byte[]{127,0,0,5};
    String TRANSIT_SWITCH_1_MAC = "fa:16:3e:d7:f1:04";

    String TRANSIT_SWTICH_2_HOST_ID = "ts-2";
    byte[] TRANSIT_SWITCH_2_IP = new byte[]{127,0,0,6};
    String TRANSIT_SWITCH_2_MAC = "fa:16:3e:d7:f1:05";

    String TRANSIT_ROUTER_1_HOST_ID = "tr-1";
    byte[] TRANSIT_ROUTER_1_IP = new byte[]{127,0,0,7};
    String TRANSIT_ROUTER_1_MAC = "fa:16:3e:d7:f1:06";

    String TRANSIT_ROUTER_2_HOST_ID = "tr-2";
    byte[] TRANSIT_ROUTER_2_IP = new byte[]{127,0,0,8};
    String TRANSIT_ROUTER_2_MAC = "fa:16:3e:d7:f1:07";

    String VNET_NAME = "veth0";

    String EP1_ID = "ep1";
    byte[] EP1_HOST_IP = new byte[]{127,0,0,1};
    String EP1_HOST_MAC = "fa:16:3e:d7:f1:00";

    String EP2_ID = "ep2";
    byte[] EP2_HOST_IP = new byte[]{127,0,0,2};
    String EP2_HOST_MAC = "fa:16:3e:d7:f1:01";

    String EP3_ID = "ep3";
    byte[] EP3_HOST_IP = new byte[]{127,0,0,3};
    String EP3_HOST_MAC = "fa:16:3e:d7:f1:02";

    String EP4_ID = "ep4";
    byte[] EP4_HOST_IP = new byte[]{127,0,0,4};
    String EP4_HOST_MAC = "fa:16:3e:d7:f1:03";
}
