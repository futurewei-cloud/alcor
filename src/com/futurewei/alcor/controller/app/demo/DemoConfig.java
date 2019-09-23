package com.futurewei.alcor.controller.app.demo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class DemoConfig {
//    String HOST_ID_PREFIX = "hostid-";
    public static boolean IS_Demo = true;

    public static long Tunnel_Id = 3000;
    public static int OVERFLOW_IP_CONVERSION = 256;
    public static byte FIRST_IP_BLOCK = (byte)(172-OVERFLOW_IP_CONVERSION);

    public static int GRPC_SERVER_PORT = 50051;
    public static BufferedWriter TIME_STAMP_WRITER;
    public static long TOTAL_TIME = 0;
    public static int TOTAL_REQUEST = 0;

    public static String gRPCServerIp = "10.213.43.166";
    public static int[] gRPCServerPortForSubnet1 = {
            50001,
            50002,
            50003,
            50004,
            50009,
            50010
    };

    static {
        try {
            TIME_STAMP_WRITER = new BufferedWriter(
                    new FileWriter("c:/temp/samplefile.txt", true)  //Set true for append mode
                );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: figure out to store integer value over 127 in a byte
    public static String TRANSIT_SWTICH_1_HOST_ID = "switchhost_1";
    public static byte[] TRANSIT_SWITCH_1_IP = new byte[]{FIRST_IP_BLOCK,17,0,15};
    public static String TRANSIT_SWITCH_1_MAC = "02:42:ac:11:00:0f";

    public static String TRANSIT_SWTICH_2_HOST_ID = "switchhost_2";
    public static byte[] TRANSIT_SWITCH_2_IP = new byte[]{FIRST_IP_BLOCK,17,0,16};
    public static String TRANSIT_SWITCH_2_MAC = "02:42:ac:11:00:10";

    public static String TRANSIT_SWTICH_3_HOST_ID = "switchhost_3";
    public static byte[] TRANSIT_SWITCH_3_IP = new byte[]{FIRST_IP_BLOCK,17,0,17};
    public static String TRANSIT_SWITCH_3_MAC = "02:42:ac:11:00:11";

    public static String TRANSIT_SWTICH_4_HOST_ID = "switchhost_4";
    public static byte[] TRANSIT_SWITCH_4_IP = new byte[]{FIRST_IP_BLOCK,17,0,18};
    public static String TRANSIT_SWITCH_4_MAC = "02:42:ac:11:00:12";

    public static String TRANSIT_ROUTER_1_HOST_ID = "routerhost_1";
    public static byte[] TRANSIT_ROUTER_1_IP = new byte[]{FIRST_IP_BLOCK,17,0,19};
    public static String TRANSIT_ROUTER_1_MAC = "02:42:ac:11:00:13";

    public static String TRANSIT_ROUTER_2_HOST_ID = "routerhost_2";
    public static byte[] TRANSIT_ROUTER_2_IP = new byte[]{FIRST_IP_BLOCK,17,0,20};
    public static String TRANSIT_ROUTER_2_MAC = "02:42:ac:11:00:14";

    public static String VNET_NAME = "veth0";

    public static String EP1_ID = "ephost_1";
    public static byte[] EP1_HOST_IP = new byte[]{FIRST_IP_BLOCK,17,0,7};
    public static String EP1_HOST_MAC = "02:42:ac:11:00:07";

    public static String EP2_ID = "ephost_2";
    public static byte[] EP2_HOST_IP = new byte[]{FIRST_IP_BLOCK,17,0,8};
    public static String EP2_HOST_MAC = "02:42:ac:11:00:08";

    public static String EP3_ID = "ephost_3";
    public static byte[] EP3_HOST_IP = new byte[]{FIRST_IP_BLOCK,17,0,9};
    public static String EP3_HOST_MAC = "02:42:ac:11:00:09";

    public static String EP4_ID = "ephost_4";
    public static byte[] EP4_HOST_IP = new byte[]{FIRST_IP_BLOCK,17,0,10};
    public static String EP4_HOST_MAC = "02:42:ac:11:00:0a";

    public static String EP5_ID = "ephost_5";
    public static byte[] EP5_HOST_IP = new byte[]{FIRST_IP_BLOCK,17,0,11};
    public static String EP5_HOST_MAC = "02:42:ac:11:00:0b";

    public static String EP6_ID = "ephost_6";
    public static byte[] EP6_HOST_IP = new byte[]{FIRST_IP_BLOCK,17,0,12};
    public static String EP6_HOST_MAC = "02:42:ac:11:00:0c";

    public static String EP7_ID = "ephost_7";
    public static byte[] EP7_HOST_IP = new byte[]{FIRST_IP_BLOCK,17,0,13};
    public static String EP7_HOST_MAC = "02:42:ac:11:00:0d";

    public static String EP8_ID = "ephost_8";
    public static byte[] EP8_HOST_IP = new byte[]{FIRST_IP_BLOCK,17,0,14};
    public static String EP8_HOST_MAC = "02:42:ac:11:00:0e";
}
