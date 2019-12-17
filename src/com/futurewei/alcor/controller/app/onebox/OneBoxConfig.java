package com.futurewei.alcor.controller.app.onebox;

import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.model.PortState;
import com.futurewei.alcor.controller.model.SubnetState;
import com.futurewei.alcor.controller.model.VpcState;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class OneBoxConfig {

    public static boolean IS_Demo = true;
    public static boolean IS_PARALLEL = true;
    public static int TEST_NUM_PORTS = 1000;

    public static String HOST_ID_PREFIX = "es8-";
    public static int GRPC_SERVER_PORT = 50005;
    public static long Tunnel_Id = 3000;

    // TODO: figure out to store integer value over 127 in a byte
    public static int OVERFLOW_IP_CONVERSION = 256;
    public static byte FIRST_IP_BLOCK = (byte)(172-OVERFLOW_IP_CONVERSION);

    public static FileWriter TIME_STAMP_FILE;
    public static BufferedWriter TIME_STAMP_WRITER;
    public static String LOG_FILE_PATH = "timestamp.log";

    public static long TOTAL_TIME = 0;
    public static int TOTAL_REQUEST = 0;
    public static long MAX_TIME = Long.MIN_VALUE;
    public static long MIN_TIME = Long.MAX_VALUE;
    public static long APP_START_TS = 0;

    public static String gRPCServerIp = "172.17.0.1"; //"10.213.43.166";
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
            File file = new File(LOG_FILE_PATH);
            if (!file.exists()) {
                file.createNewFile();
            }

            TIME_STAMP_FILE = new FileWriter(file);
            TIME_STAMP_WRITER = new BufferedWriter(TIME_STAMP_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /////////////////////////////////
    // Virtual network resources
    /////////////////////////////////
    public static String VETH_NAME = "veth0";

    public static String projectId = "dbf72700-5106-4a7a-918f-a016853911f8";
    public static String vpcId = "99d9d709-8478-4b46-9f3f-2206b1023fd3";
    public static String subnetId = "9fd87d97-7164-4f6b-b116-e9fdf30c6339";

    public static String subnet1Id = "d973934b-93e8-42fa-ac91-bf0cdb84fffc";
    public static String subnet2Id = "8cb94df3-05bd-45d1-95c0-1ad75f929810";
    public static String ep1Id = "89e72582-b4fc-4e4e-b46a-6eee650e03f5";
    public static String ep2Id = "34bf0cec-0969-4635-b9a9-dd32611f35a4";
    public static String ep3Id = "64353fd7-b60c-4108-93ff-ecaa6b63a6a3";
    public static String ep4Id = "cae2df90-4a50-437e-a3f2-e3b742c8fbf8";
    public static String ep5Id = "364d2bbd-2def-4c70-9965-9ffd2165f43a";
    public static String ep6Id = "c60fe503-88a2-4198-a3be-85c197acd9db";
    public static String ep7Id = "38e45f95-5ea7-4d0a-9027-886febc27bdc";
    public static String ep8Id = "b81abf49-87ab-4a58-b457-93dc5a0dabac";

    public static VpcState customerVpcState =
            new VpcState(projectId, vpcId,
                    "SuperVpc",
                    "10.0.0.0/16");

    // Large VPC
    public static SubnetState customerSubnetState = new SubnetState(projectId, vpcId, subnetId,
            "Subnet1",
            "10.0.0.0/20",
            "10.0.0.5");

    public static SubnetState customerSubnetState1 = new SubnetState(projectId, vpcId, subnet1Id,
            "Subnet1",
            "10.0.0.0/24",
            "10.0.0.5");
    public static SubnetState customerSubnetState2 = new SubnetState(projectId, vpcId, subnet2Id,
            "Subnet2",
            "10.0.1.0/24",
            "10.0.1.5");

    public static PortState[] customerPortStateForSubnet1 = {
            new PortState(projectId, subnet1Id, ep1Id,
                    "89e72582", //DemoConfig.EP1_ID,
                    "0e:73:ae:c8:87:00",
                    OneBoxConfig.VETH_NAME,
                    new String[]{"10.0.0.1"}),
            new PortState(projectId, subnet1Id, ep2Id,
                    "34bf0cec",
                    "0e:73:ae:c8:87:01",
                    OneBoxConfig.VETH_NAME,
                    new String[]{"10.0.0.2"}),
            new PortState(projectId, subnet1Id, ep3Id,
                    "64353fd7",
                    "0e:73:ae:c8:87:02",
                    OneBoxConfig.VETH_NAME,
                    new String[]{"10.0.0.3"}),
            new PortState(projectId, subnet1Id, ep4Id,
                    "cae2df90",
                    "0e:73:ae:c8:87:03",
                    OneBoxConfig.VETH_NAME,
                    new String[]{"10.0.0.4"})
    };

    public static PortState[] customerPortStateForSubnet2 = {
            new PortState(projectId, subnet2Id, ep5Id,
                    "364d2bbd",
                    "0e:73:ae:c8:87:04",
                    OneBoxConfig.VETH_NAME,
                    new String[]{"10.0.1.1"}),
            new PortState(projectId, subnet2Id, ep6Id,
                    "c60fe503",
                    "0e:73:ae:c8:87:05",
                    OneBoxConfig.VETH_NAME,
                    new String[]{"10.0.1.2"}),
            new PortState(projectId, subnet2Id, ep7Id,
                    "38e45f95",
                    "0e:73:ae:c8:87:06",
                    OneBoxConfig.VETH_NAME,
                    new String[]{"10.0.1.3"}),
            new PortState(projectId, subnet2Id, ep8Id,
                    "b81abf49",
                    "0e:73:ae:c8:87:07",
                    OneBoxConfig.VETH_NAME,
                    new String[]{"10.0.1.4"})
    };

    public static List<PortState> customerPortStates;

    /////////////////////////////////
    // Physical network resources
    /////////////////////////////////
    public static String GATEWAY_MAC_ADDRESS = "02:42:ac:11:00:0d"; //"0e:73:ae:c8:FF:FF";

    public static String TRANSIT_SWTICH_1_HOST_ID = "switchhost_0";
    public static byte[] TRANSIT_SWITCH_1_IP = new byte[]{FIRST_IP_BLOCK,17,0,11};
    public static String TRANSIT_SWITCH_1_MAC = "02:42:ac:11:00:0b";

    public static String TRANSIT_SWTICH_2_HOST_ID = "switchhost_1";
    public static byte[] TRANSIT_SWITCH_2_IP = new byte[]{FIRST_IP_BLOCK,17,0,12};
    public static String TRANSIT_SWITCH_2_MAC = "02:42:ac:11:00:0c";

    public static String TRANSIT_SWTICH_3_HOST_ID = "switchhost_2";
    public static byte[] TRANSIT_SWITCH_3_IP = new byte[]{FIRST_IP_BLOCK,17,0,13};
    public static String TRANSIT_SWITCH_3_MAC = "02:42:ac:11:00:0d";

    public static String TRANSIT_SWTICH_4_HOST_ID = "switchhost_3";
    public static byte[] TRANSIT_SWITCH_4_IP = new byte[]{FIRST_IP_BLOCK,17,0,14};
    public static String TRANSIT_SWITCH_4_MAC = "02:42:ac:11:00:0e";

    public static String TRANSIT_ROUTER_1_HOST_ID = "routerhost_0";
    public static byte[] TRANSIT_ROUTER_1_IP = new byte[]{FIRST_IP_BLOCK,17,0,15};
    public static String TRANSIT_ROUTER_1_MAC = "02:42:ac:11:00:0f";

    public static String TRANSIT_ROUTER_2_HOST_ID = "routerhost_1";
    public static byte[] TRANSIT_ROUTER_2_IP = new byte[]{FIRST_IP_BLOCK,17,0,16};
    public static String TRANSIT_ROUTER_2_MAC = "02:42:ac:11:00:10";

//    public static HostInfo[] transitRouterHosts = {
//            new HostInfo("vpc1-transit-router1", "transit router1 host", DemoConfig.TRANSIT_ROUTER_1_IP, DemoConfig.TRANSIT_ROUTER_1_MAC),
//            new HostInfo("vpc1-transit-router2", "transit router2 host", DemoConfig.TRANSIT_ROUTER_2_IP, DemoConfig.TRANSIT_ROUTER_2_MAC)
//    };

    public static HostInfo[] transitSwitchHostsForSubnet1 = {
            new HostInfo("subnet1-transit-switch1","transit switch1 host for subnet1", OneBoxConfig.TRANSIT_SWITCH_1_IP, OneBoxConfig.TRANSIT_SWITCH_1_MAC, OneBoxConfig.gRPCServerPortForSubnet1[4]),
            new HostInfo("subnet1-transit-switch2","transit switch2 host for subnet1", OneBoxConfig.TRANSIT_SWITCH_2_IP, OneBoxConfig.TRANSIT_SWITCH_2_MAC, OneBoxConfig.gRPCServerPortForSubnet1[5])
    };

    public static HostInfo[] transitSwitchHostsForSubnet2 = {
            new HostInfo("subnet2-transit-switch1","transit switch1 host for subnet2", OneBoxConfig.TRANSIT_SWITCH_3_IP, OneBoxConfig.TRANSIT_SWITCH_3_MAC),
            new HostInfo("subnet2-transit-switch2","transit switch2 host for subnet2", OneBoxConfig.TRANSIT_SWITCH_4_IP, OneBoxConfig.TRANSIT_SWITCH_4_MAC)
    };

//    public static HostInfo[] transitRouterHosts = {
//            new HostInfo("vpc1-transit-router1", "transit router1 host", new byte[]{FIRST_IP_BLOCK,17,0,(byte)(15)}, "02:42:ac:11:00:0f", 50011),
//            new HostInfo("vpc1-transit-router2", "transit router2 host", new byte[]{FIRST_IP_BLOCK,17,0,(byte)(16)}, "02:42:ac:11:00:10", 50012)
//    };
    // Large VPC
    // (byte)(205-OVERFLOW_IP_CONVERSION)
    public static HostInfo[] transitRouterHosts = {
            new HostInfo("vpc1-transit-router1", "transit router1 host", new byte[]{FIRST_IP_BLOCK,17,0,(byte)(6)}, "02:42:ac:11:00:06", 50001)
    };

    public static HostInfo[] transitSwitchHosts = {
            new HostInfo("switchhost_0","switchhost_0", new byte[]{FIRST_IP_BLOCK,17,0, (byte)(7)}, "02:42:ac:11:00:07", 50002),
            new HostInfo("switchhost_1","switchhost_1", new byte[]{FIRST_IP_BLOCK,17,0, (byte)(8)}, "02:42:ac:11:00:08", 50003),
            new HostInfo("switchhost_2","switchhost_2", new byte[]{FIRST_IP_BLOCK,17,0, (byte)(9)}, "02:42:ac:11:00:09", 50004)
    };

    public static String EP1_ID = "ephost_0";
    public static byte[] EP1_HOST_IP = new byte[]{FIRST_IP_BLOCK,17,0,3};
    public static String EP1_HOST_MAC = "02:42:ac:11:00:03";

    public static String EP2_ID = "ephost_1";
    public static byte[] EP2_HOST_IP = new byte[]{FIRST_IP_BLOCK,17,0,4};
    public static String EP2_HOST_MAC = "02:42:ac:11:00:04";

    public static String EP3_ID = "ephost_2";
    public static byte[] EP3_HOST_IP = new byte[]{FIRST_IP_BLOCK,17,0,5};
    public static String EP3_HOST_MAC = "02:42:ac:11:00:05";

    public static String EP4_ID = "ephost_3";
    public static byte[] EP4_HOST_IP = new byte[]{FIRST_IP_BLOCK,17,0,6};
    public static String EP4_HOST_MAC = "02:42:ac:11:00:06";

    public static String EP5_ID = "ephost_4";
    public static byte[] EP5_HOST_IP = new byte[]{FIRST_IP_BLOCK,17,0,7};
    public static String EP5_HOST_MAC = "02:42:ac:11:00:07";

    public static String EP6_ID = "ephost_5";
    public static byte[] EP6_HOST_IP = new byte[]{FIRST_IP_BLOCK,17,0,8};
    public static String EP6_HOST_MAC = "02:42:ac:11:00:08";

    public static String EP7_ID = "ephost_6";
    public static byte[] EP7_HOST_IP = new byte[]{FIRST_IP_BLOCK,17,0,9};
    public static String EP7_HOST_MAC = "02:42:ac:11:00:09";

    public static String EP8_ID = "ephost_7";
    public static byte[] EP8_HOST_IP = new byte[]{FIRST_IP_BLOCK,17,0,10};
    public static String EP8_HOST_MAC = "02:42:ac:11:00:0a";

    public static HostInfo[] epHostForSubnet1 = {
            new HostInfo("subnet1-ep1", "ep1 host", OneBoxConfig.EP1_HOST_IP, OneBoxConfig.EP1_HOST_MAC, OneBoxConfig.gRPCServerPortForSubnet1[0]),
            new HostInfo("subnet1-ep2", "ep2 host", OneBoxConfig.EP2_HOST_IP, OneBoxConfig.EP2_HOST_MAC, OneBoxConfig.gRPCServerPortForSubnet1[1]),
            new HostInfo("subnet1-ep3", "ep3 host", OneBoxConfig.EP3_HOST_IP, OneBoxConfig.EP3_HOST_MAC, OneBoxConfig.gRPCServerPortForSubnet1[2]),
            new HostInfo("subnet1-ep4", "ep4 host", OneBoxConfig.EP4_HOST_IP, OneBoxConfig.EP4_HOST_MAC, OneBoxConfig.gRPCServerPortForSubnet1[3]),
    };

    public static HostInfo[] epHostForSubnet2 = {
            new HostInfo("subnet2-ep1", "ep5 host", OneBoxConfig.EP5_HOST_IP, OneBoxConfig.EP5_HOST_MAC),
            new HostInfo("subnet2-ep2", "ep6 host", OneBoxConfig.EP6_HOST_IP, OneBoxConfig.EP6_HOST_MAC),
            new HostInfo("subnet2-ep3", "ep7 host", OneBoxConfig.EP7_HOST_IP, OneBoxConfig.EP7_HOST_MAC),
            new HostInfo("subnet2-ep4", "ep8 host", OneBoxConfig.EP8_HOST_IP, OneBoxConfig.EP8_HOST_MAC),
    };

    public static int epHostCounter = 0;
    public static List<HostInfo> epHosts;
    public static int epCounter = 0;
    public static int EP_PER_HOST = 1;
}
