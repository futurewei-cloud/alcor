package com.futurewei.alioth.controller.comm.config;

public interface DemoConfig {
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
}
