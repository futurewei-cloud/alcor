package com.futurewei.alcor.dataplane.utils;

import com.futurewei.alcor.dataplane.config.UnitTestConfig;
import com.futurewei.alcor.dataplane.entity.*;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GoalStateManagerTest {

    static Gson gson = null;
    static GoalStateManager goalStateManager = null;
    static DataPlaneManagerUtil util = null;

    @BeforeClass
    public static void setUp() {
        util = new DataPlaneManagerUtil();
        goalStateManager = new GoalStateManager();
        ExclusionStrategy myExclusionStrategy =
                new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes fa) {
                        return fa.getName().equals("tenantId");
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                };
        gson = new GsonBuilder().setExclusionStrategies(myExclusionStrategy).create();
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Test Failure1 During Integration With Nova
     */
    @Test
    public void testFailure1DuringIntegrationWithNova() {
        String input = UnitTestConfig.testFailure1DuringIntegrationWithNova_input;
        String output = UnitTestConfig.testFailure1DuringIntegrationWithNova_output;
        check(input, output);
    }

    /**
     * Test Failure2 During Integration With Nova
     */
    @Test
    public void testFailure2DuringIntegrationWithNova() {
        String input = UnitTestConfig.testFailure2DuringIntegrationWithNova_input;
        String output = UnitTestConfig.testFailure2DuringIntegrationWithNova_output;
        check(input, output);
    }

    /**
     * Scenario: Create first Port (P1) without neighbor at Host 1
     */
    @Test
    public void scenario_createFirstPortP1AtHost1_FastPathOnly() {
        String input = UnitTestConfig.scenario_createFirstPortP1AtHost1_FastPathOnly_input;
        String output = UnitTestConfig.scenario_createFirstPortP1AtHost1_FastPathOnly_output;
        check(input, output);
    }

    /**
     * Scenario: Create first Port (P1) with neighbor at Host 1
     */
    @Test
    public void scenario_createFirstPortP1WithNeighborAtHost1_FastPathOnly() {
        String input = UnitTestConfig.scenario_createFirstPortP1WithNeighborAtHost1_FastPathOnly_input;
        String output = UnitTestConfig.scenario_createFirstPortP1WithNeighborAtHost1_FastPathOnly_output;
        check(input, output);
    }

    /**
     * Scenario: Create two Ports (P1,P2) without neighbor at Host 1
     */
    @Test
    public void scenario2v1_SingleFP() {
        String input = UnitTestConfig.scenario2v1_SingleFP_input;
        String output = UnitTestConfig.scenario2v1_SingleFP_output;
        check(input, output);
    }

    /**
     * Scenario: Create two Ports (P2,P3) with neighbor (P1) at Host1
     */
    @Test
    public void scenarioNeighbor2v1_SingleFP() {
        String input = UnitTestConfig.scenarioNeighbor2v1_SingleFP_input;
        String output = UnitTestConfig.scenarioNeighbor2v1_SingleFP_output;
        check(input, output);
    }

    /**
     * Scenario: Create two Ports (P1,P2) without neighbor at Host1 and create two Ports (P3,P4) without neighbor at Host2 at same time
     */
    @Test
    public void scenarioBulkCreate2v2_SingleFP() {
        String input = UnitTestConfig.scenarioBulkCreate2v2_SingleFP_input;
        String output = UnitTestConfig.scenarioBulkCreate2v2_SingleFP_output;
        check(input, output);
    }

    /**
     * Scenario: Create Port(P1) with neighbor(P3) and Port(P2) with neighbor(P4) at Host1
     * and create Port(P3) with neighbor(P1) and Port(P4) with neighbor(P2)r at Host2 at same time
     */
    @Test
    public void scenarioBulkNeighbor2v2_SingleFP() {
        String input = UnitTestConfig.scenarioBulkNeighbor2v2_SingleFP_input;
        String output = UnitTestConfig.scenarioBulkNeighbor2v2_SingleFP_output;
        check(input, output);
    }

    /**
     * Scenario: Resource Request With Updated All fields
     */
    @Test
    public void scenarioResourceRequestWithUpdatedAllfields() {
        String input = UnitTestConfig.scenarioResourceRequestWithUpdatedAllfields_input;
        String output = UnitTestConfig.scenarioResourceRequestWithUpdatedAllfields_output;
        check(input, output);
    }

    /**
     * dhcp test
     */
    @Test
    public void dhcpTest() {
        String input = UnitTestConfig.dhcpTest_input;
        String output = UnitTestConfig.dhcpTest_output;
        check(input, output);
    }

    /**
     * Scenario: L3 - Create first Port (P1) without neighbor at Host 1
     */
    @Test
    public void scenario_L3_createFirstPortP1AtHost1_FastPathOnly() throws Exception {
        NetworkConfiguration input = util.autoGenerateUTsInput(0, 2, 1, 1, 1, 2, 3, true, true, true, false, 0, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput(0, 2, 1, 1, 1, 2, 3, true,true, true, false, 0, true);
        L3Check(input, output);
    }

    /**
     * Scenario: L3 - Create first Port (P2) with neighbor P1 at Host 1
     */
    @Test
    public void scenario_L3_createFirstPortP2WithNeighborP1AtHost1_FastPathOnly() throws Exception {
        NetworkConfiguration input = util.autoGenerateUTsInput(0, 2, 1, 1, 1, 2, 3, false,false, false, true, 1, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput(0, 2, 1, 1, 1, 2, 3, false,false, false, true, 1, true);
        L3Check(input, output);
    }

    /**
     * Scenario: L3 - Create Ports (P1, P2) without neighbor at Host 1, P1 and P2 are associated with different subnet
     */
    @Test
    public void scenario_L3_createPortP1P2WithoutNeighborAtHost1_FastPathOnly() throws Exception {
        NetworkConfiguration input = util.autoGenerateUTsInput(0, 2, 2, 1, 2, 2, 0, false,false, false, false, 0, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput(0, 2, 2, 1, 2, 2, 0, false,false, false, false, 0, true);
        L3Check(input, output);
    }

    /**
     * Scenario: L3 - Create Ports (P2, P3) with neighbor P1 at Host 1, P2 and P3 are associated with different subnet
     */
    @Test
    public void scenario_L3_createPortP2P3WithNeighborP1AtHost1_FastPathOnly() throws Exception {
        NetworkConfiguration input = util.autoGenerateUTsInput(0, 2, 2, 1, 2, 2, 0, false,false, false, true, 1, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput(0, 2, 2, 1, 2, 2, 0, false,false, false, true, 1, true);
        L3Check(input, output);
    }

    /**
     * Scenario: L3 - Create Port (P1) without neighbor at Host 1 and Port (P2) without neighbor at Host 2, P1 and P2 are associated with same subnet
     */
    @Test
    public void scenario_L3_createPortP1P2WithoutNeighborAtHost1AndHost2_FastPathOnly() throws Exception {
        NetworkConfiguration input = util.autoGenerateUTsInput(0, 2, 1, 2, 2, 2, 0, false,false, false, false, 0, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput(0, 2, 1, 2, 2, 2, 0, false,false, false, false, 0, true);
        L3Check(input, output);
    }

    /**
     * Scenario: L3 - Create Port (P1) with neighbor at Host 1 and Port (P2) with neighbor at Host 2, P1 and P2 are associated with same subnet
     */
    @Test
    public void scenario_L3_createPortP1P2WithNeighborAtHost1AndHost2_FastPathOnly() throws Exception {
        NetworkConfiguration input = util.autoGenerateUTsInput(0, 2, 1, 2, 2, 2, 0, false,false, false, true, 1, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput(0, 2, 1, 2, 2, 2, 0, false,false, false, true, 1, true);
        L3Check(input, output);
    }

    /**
     * Scenario: L3_Customize_Second_Version - Create first Port (P1) without neighbor at Host 1
     */
    @Test
    public void scenario_L3_Customize_Second_Version_createFirstPortP1AtHost1_FastPathOnly() throws Exception {
        // configure Map<String(bindingHostIP), UTPortWithSubnetAndIPMapping> createPortsMap
        Map<String, List<UTPortWithSubnetAndIPMapping>> createPortsMap = new HashMap<>();
        List<UTPortWithSubnetAndIPMapping> mapList = new ArrayList<>();
        UTPortWithSubnetAndIPMapping mapping = new UTPortWithSubnetAndIPMapping();
        List<PortEntity.FixedIp> fixedIps = new ArrayList<>();
        PortEntity.FixedIp fixedIp = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785940", "192.168.2.20");
        fixedIps.add(fixedIp);

        mapping.setBindingHostId("ephost_0");
        mapping.setFixedIps(fixedIps);
        mapping.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d0");
        mapping.setPortMacAddress("86:ea:77:ad:52:50");
        mapping.setPortName("test_cni_port1");
        mapping.setVethName("veth0");

        mapList.add(mapping);
        createPortsMap.put("10.213.43.187", mapList);

        // configure Map<String(bindingHostIP), UTPortWithSubnetAndIPMapping> existPortsMap
        Map<String, List<UTPortWithSubnetAndIPMapping>> existPortsMap = new HashMap<>();
        List<UTPortWithSubnetAndIPMapping> existPortsMapList = new ArrayList<>();
        existPortsMap.put("10.213.43.187", existPortsMapList);
        existPortsMap.put("10.213.43.187", mapList);

        // configure List<UTSubnetInfo>
        List<UTSubnetInfo> UTSubnets = new ArrayList<>();
        UTSubnetInfo utSubnetInfo = new UTSubnetInfo();
        utSubnetInfo.setSubnetCidr("192.168.2.0/24");
        utSubnetInfo.setSubnetGatewayIP("192.168.2.20");
        utSubnetInfo.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785940");
        utSubnetInfo.setSubnetName("test_subnet0");
        utSubnetInfo.setTunnelId(Long.parseLong("88888"));
        UTSubnets.add(utSubnetInfo);

        // configure List<UTL3NeighborInfoMapping>
        List<UTL3NeighborInfoMapping> L3NeighborInfoMapping = new ArrayList<>();
        UTL3NeighborInfoMapping utl3NeighborInfoMapping = new UTL3NeighborInfoMapping();
        utl3NeighborInfoMapping.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785940");
        utl3NeighborInfoMapping.setIPsInSubnet(new ArrayList<>(){{add(new UTIPInfo("192.168.2.20", false));}});
        L3NeighborInfoMapping.add(utl3NeighborInfoMapping);

        // configure List<UTNeighborInfoDetail>
        Map<String, UTNeighborInfoDetail> neighborInfoDetails = new HashMap<>();

        NetworkConfiguration input = util.autoGenerateUTsInput_MoreCustomizableScenarios(0, 2, createPortsMap, UTSubnets, L3NeighborInfoMapping, true, true, true, false, neighborInfoDetails, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput_MoreCustomizableScenarios(0, 2, createPortsMap, existPortsMap, UTSubnets, L3NeighborInfoMapping, true,true, true, false, neighborInfoDetails, true);
        L3Check_Second_Version(input, output, existPortsMap);
    }

    /**
     * Scenario: L3_Customize_Second_Version - Create Ports (P1, P2) without neighbor at Host 1, P1 and P2 are associated with same subnet
     */
    @Test
    public void scenario_L3_Customize_Second_Version_createPortP1P2WithoutNeighborAtHost1_FastPathOnly() throws Exception {
        // configure Map<String(bindingHostIP), UTPortWithSubnetAndIPMapping>
        Map<String, List<UTPortWithSubnetAndIPMapping>> createPortsMap = new HashMap<>();
        List<UTPortWithSubnetAndIPMapping> mapList = new ArrayList<>();

        // P1 configuration
        UTPortWithSubnetAndIPMapping mapping1 = new UTPortWithSubnetAndIPMapping();
        List<PortEntity.FixedIp> fixedIps1 = new ArrayList<>();
        PortEntity.FixedIp fixedIp1 = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785940", "192.168.2.20");
        fixedIps1.add(fixedIp1);

        mapping1.setBindingHostId("ephost_0");
        mapping1.setFixedIps(fixedIps1);
        mapping1.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d0");
        mapping1.setPortMacAddress("86:ea:77:ad:52:50");
        mapping1.setPortName("test_cni_port1");
        mapping1.setVethName("veth0");

        mapList.add(mapping1);

        // P2 configuration
        UTPortWithSubnetAndIPMapping mapping2 = new UTPortWithSubnetAndIPMapping();
        List<PortEntity.FixedIp> fixedIps2 = new ArrayList<>();
        PortEntity.FixedIp fixedIp2 = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785940", "192.168.2.21");
        fixedIps2.add(fixedIp2);

        mapping2.setBindingHostId("ephost_0");
        mapping2.setFixedIps(fixedIps2);
        mapping2.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d1");
        mapping2.setPortMacAddress("86:ea:77:ad:52:51");
        mapping2.setPortName("test_cni_port2");
        mapping2.setVethName("veth0");

        mapList.add(mapping2);

        createPortsMap.put("10.213.43.187", mapList);

        // configure Map<String(bindingHostIP), UTPortWithSubnetAndIPMapping> existPortsMap
        Map<String, List<UTPortWithSubnetAndIPMapping>> existPortsMap = new HashMap<>();
        List<UTPortWithSubnetAndIPMapping> existPortsMapList = new ArrayList<>();
        existPortsMap.put("10.213.43.187", existPortsMapList);
        existPortsMap.put("10.213.43.187", mapList);

        // configure List<UTSubnetInfo>
        List<UTSubnetInfo> UTSubnets = new ArrayList<>();
        UTSubnetInfo utSubnetInfo = new UTSubnetInfo();
        utSubnetInfo.setSubnetCidr("192.168.2.0/24");
        utSubnetInfo.setSubnetGatewayIP("192.168.2.20");
        utSubnetInfo.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785940");
        utSubnetInfo.setSubnetName("test_subnet0");
        utSubnetInfo.setTunnelId(Long.parseLong("88888"));
        UTSubnets.add(utSubnetInfo);

        // configure List<UTL3NeighborInfoMapping>
        List<UTL3NeighborInfoMapping> L3NeighborInfoMapping = new ArrayList<>();
        UTL3NeighborInfoMapping utl3NeighborInfoMapping = new UTL3NeighborInfoMapping();
        utl3NeighborInfoMapping.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785940");
        utl3NeighborInfoMapping.setIPsInSubnet(new ArrayList<>(){{add(new UTIPInfo("192.168.2.20", false));add(new UTIPInfo("192.168.2.21", false));}});
        L3NeighborInfoMapping.add(utl3NeighborInfoMapping);

        // configure List<UTNeighborInfoDetail>
        Map<String, UTNeighborInfoDetail> neighborInfoDetails = new HashMap<>();

        NetworkConfiguration input = util.autoGenerateUTsInput_MoreCustomizableScenarios(0, 2, createPortsMap, UTSubnets, L3NeighborInfoMapping, true, true, true, false, neighborInfoDetails, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput_MoreCustomizableScenarios(0, 2, createPortsMap, existPortsMap, UTSubnets, L3NeighborInfoMapping, true,true, true, false, neighborInfoDetails, true);
        L3Check_Second_Version(input, output, existPortsMap);
    }

    /**
     * Scenario: L3_Customize_Second_Version - Create Port P6 with neighbor at Host 1,
     * (P1, P2) are in Host 1, (P3, P4) are in Host 2
     * (P1, P2) are associated with same subnet, (P3, P4) are associated with same subnet
     * Host1: 2.2, 3.4; Host2: 2.3, 3.2, 3.3
     */
    @Test
    public void scenario_L3_Customize_Second_Version_createPortP6WithNeighborAtHost1_FastPathOnly() throws Exception {
        // configure Map<String(bindingHostIP), UTPortWithSubnetAndIPMapping>
        Map<String, List<UTPortWithSubnetAndIPMapping>> createPortsMap = new HashMap<>();
        List<UTPortWithSubnetAndIPMapping> mapList = new ArrayList<>();

        // P6 configuration
        UTPortWithSubnetAndIPMapping mapping1 = new UTPortWithSubnetAndIPMapping();
        List<PortEntity.FixedIp> fixedIps1 = new ArrayList<>();
        PortEntity.FixedIp fixedIp1 = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785940", "192.168.3.5");
        fixedIps1.add(fixedIp1);

        mapping1.setBindingHostId("ephost_0");
        mapping1.setFixedIps(fixedIps1);
        mapping1.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d5");
        mapping1.setPortMacAddress("86:ea:77:ad:52:50");
        mapping1.setPortName("test_cni_port6");
        mapping1.setVethName("veth0");

        mapList.add(mapping1);

        createPortsMap.put("10.213.43.187", mapList);

        // configure Map<String(bindingHostIP), UTPortWithSubnetAndIPMapping> existPortsMap
        Map<String, List<UTPortWithSubnetAndIPMapping>> existPortsMap = new HashMap<>();
        List<UTPortWithSubnetAndIPMapping> existPortsMapList1 = new ArrayList<>();
        List<UTPortWithSubnetAndIPMapping> existPortsMapList2 = new ArrayList<>();

        // P1 configuration
        UTPortWithSubnetAndIPMapping existMapping1 = new UTPortWithSubnetAndIPMapping();
        List<PortEntity.FixedIp> existFixedIps1 = new ArrayList<>();
        PortEntity.FixedIp existFixedIp1 = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785940", "192.168.2.2");
        existFixedIps1.add(existFixedIp1);

        existMapping1.setBindingHostId("ephost_0");
        existMapping1.setFixedIps(existFixedIps1);
        existMapping1.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d0");
        existMapping1.setPortMacAddress("86:ea:77:ad:52:55");
        existMapping1.setPortName("test_cni_port1");
        existMapping1.setVethName("veth0");
        existPortsMapList1.add(existMapping1);

        // P2 configuration
        UTPortWithSubnetAndIPMapping existMapping2 = new UTPortWithSubnetAndIPMapping();
        List<PortEntity.FixedIp> existFixedIps2 = new ArrayList<>();
        PortEntity.FixedIp existFixedIp2 = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785940", "192.168.3.4");
        existFixedIps2.add(existFixedIp2);

        existMapping2.setBindingHostId("ephost_0");
        existMapping2.setFixedIps(existFixedIps2);
        existMapping2.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d3");
        existMapping2.setPortMacAddress("86:ea:77:ad:52:55");
        existMapping2.setPortName("test_cni_port2");
        existMapping2.setVethName("veth0");
        existPortsMapList1.add(existMapping2);

        // P3 configuration
        UTPortWithSubnetAndIPMapping existMapping3 = new UTPortWithSubnetAndIPMapping();
        List<PortEntity.FixedIp> existFixedIps3 = new ArrayList<>();
        PortEntity.FixedIp existFixedIp3 = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785941", "192.168.3.2");
        existFixedIps3.add(existFixedIp3);

        existMapping3.setBindingHostId("ephost_1");
        existMapping3.setFixedIps(existFixedIps3);
        existMapping3.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d2");
        existMapping3.setPortMacAddress("86:ea:77:ad:52:58");
        existMapping3.setPortName("test_cni_port3");
        existMapping3.setVethName("veth0");
        existPortsMapList2.add(existMapping3);

        // P4 configuration
        UTPortWithSubnetAndIPMapping existMapping4 = new UTPortWithSubnetAndIPMapping();
        List<PortEntity.FixedIp> existFixedIps4 = new ArrayList<>();
        PortEntity.FixedIp existFixedIp4_1 = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785941", "192.168.3.3");
        PortEntity.FixedIp existFixedIp4_2 = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785941", "192.168.2.3");
        existFixedIps4.add(existFixedIp4_1);
        existFixedIps4.add(existFixedIp4_2);

        existMapping4.setBindingHostId("ephost_1");
        existMapping4.setFixedIps(existFixedIps4);
        existMapping4.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d1");
        existMapping4.setPortMacAddress("86:ea:77:ad:52:58");
        existMapping4.setPortName("test_cni_port4");
        existMapping4.setVethName("veth0");
        existPortsMapList2.add(existMapping4);

        existPortsMap.put("10.213.43.187", existPortsMapList1);
        existPortsMap.put("10.213.43.187", mapList);
        existPortsMap.put("10.213.43.163", existPortsMapList2);

        // configure List<UTSubnetInfo>
        List<UTSubnetInfo> UTSubnets = new ArrayList<>();
        UTSubnetInfo utSubnetInfo1 = new UTSubnetInfo();
        utSubnetInfo1.setSubnetCidr("192.168.2.0/24");
        utSubnetInfo1.setSubnetGatewayIP("192.168.2.20");
        utSubnetInfo1.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785940");
        utSubnetInfo1.setSubnetName("test_subnet0");
        utSubnetInfo1.setTunnelId(Long.parseLong("88888"));
        UTSubnets.add(utSubnetInfo1);

        UTSubnetInfo utSubnetInfo2 = new UTSubnetInfo();
        utSubnetInfo2.setSubnetCidr("192.168.2.0/24");
        utSubnetInfo2.setSubnetGatewayIP("192.168.2.21");
        utSubnetInfo2.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785941");
        utSubnetInfo2.setSubnetName("test_subnet1");
        utSubnetInfo2.setTunnelId(Long.parseLong("88889"));
        UTSubnets.add(utSubnetInfo2);

        // configure List<UTL3NeighborInfoMapping>
        List<UTL3NeighborInfoMapping> L3NeighborInfoMapping = new ArrayList<>();
        UTL3NeighborInfoMapping utl3NeighborInfoMapping1 = new UTL3NeighborInfoMapping();
        utl3NeighborInfoMapping1.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785940");
        utl3NeighborInfoMapping1.setIPsInSubnet(new ArrayList<>(){{add(new UTIPInfo("192.168.2.2", true));
            add(new UTIPInfo("192.168.3.4", true));add(new UTIPInfo("192.168.3.5", false));}});
        L3NeighborInfoMapping.add(utl3NeighborInfoMapping1);

        UTL3NeighborInfoMapping utl3NeighborInfoMapping2 = new UTL3NeighborInfoMapping();
        utl3NeighborInfoMapping2.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785941");
        utl3NeighborInfoMapping2.setIPsInSubnet(new ArrayList<>(){{add(new UTIPInfo("192.168.2.3", true));
            add(new UTIPInfo("192.168.3.2", true));add(new UTIPInfo("192.168.3.3", true)); }});
        L3NeighborInfoMapping.add(utl3NeighborInfoMapping2);

        // configure Map<String, UTNeighborInfoDetail>, key - port_IP
        Map<String, UTNeighborInfoDetail> neighborInfoDetails = new HashMap<>();

        UTNeighborInfoDetail detail1 = new UTNeighborInfoDetail("f37810eb-7f83-45fa-a4d4-1b31e75399d0",
                "86:ea:77:ad:52:55",
                "ephost_0",
                "10.213.43.187");
        neighborInfoDetails.put( "192.168.2.2", detail1);
        UTNeighborInfoDetail detail2 = new UTNeighborInfoDetail("f37810eb-7f83-45fa-a4d4-1b31e75399d1",
                "86:ea:77:ad:52:57",
                "ephost_1",
                "10.213.43.163");
        neighborInfoDetails.put( "192.168.2.3", detail2);
        UTNeighborInfoDetail detail3 = new UTNeighborInfoDetail("f37810eb-7f83-45fa-a4d4-1b31e75399d2",
                "86:ea:77:ad:52:58",
                "ephost_1",
                "10.213.43.163");
        neighborInfoDetails.put( "192.168.3.2", detail3);
        UTNeighborInfoDetail detail4 = new UTNeighborInfoDetail("f37810eb-7f83-45fa-a4d4-1b31e75399d1",
                "86:ea:77:ad:52:58",
                "ephost_1",
                "10.213.43.163");
        neighborInfoDetails.put( "192.168.3.3", detail4);
        UTNeighborInfoDetail detail5 = new UTNeighborInfoDetail("f37810eb-7f83-45fa-a4d4-1b31e75399d3",
                "86:ea:77:ad:52:55",
                "ephost_0",
                "10.213.43.187");
        neighborInfoDetails.put( "192.168.3.4", detail5);


        NetworkConfiguration input = util.autoGenerateUTsInput_MoreCustomizableScenarios(0, 2, createPortsMap, UTSubnets, L3NeighborInfoMapping, true, true, true, true, neighborInfoDetails, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput_MoreCustomizableScenarios(0, 2, createPortsMap, existPortsMap, UTSubnets, L3NeighborInfoMapping, true,true, true, true, neighborInfoDetails, true);
        L3Check_Second_Version(input, output, existPortsMap);
    }

    private void check(String input, String output) {

        NetworkConfiguration networkConfiguration = gson.fromJson(input, NetworkConfiguration.class);
        Map<String, Goalstate.GoalState> goalStateHashMap =
                new Gson().fromJson(output, new TypeToken<Map<String, Goalstate.GoalState>>() {
                }.getType());

        Map<String, Goalstate.GoalState> goalStateHashMap1 =
                gson.fromJson(output, new TypeToken<Map<String, Goalstate.GoalState>>() {
                }.getType());

         Map<String, Goalstate.GoalState> stringGoalStateMap=null;
        try {
            stringGoalStateMap = goalStateManager.transformNorthToSouth(networkConfiguration).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        assertEquals(goalStateHashMap.keySet().toString(), stringGoalStateMap.keySet().toString());
        assertEquals(goalStateHashMap.values().size(), stringGoalStateMap.values().size());
        assertEquals(
                goalStateHashMap
                        .get(goalStateHashMap.keySet().iterator().next())
                        .getPortStatesList()
                        .size(),
                stringGoalStateMap
                        .get(stringGoalStateMap.keySet().iterator().next())
                        .getPortStatesList()
                        .size());
    }

    private void L3Check(NetworkConfiguration input, Map<String, Goalstate.GoalState> output) {

        Map<String, Goalstate.GoalState> stringGoalStateMap = null;
        try {
            stringGoalStateMap = goalStateManager.transformNorthToSouth(input).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        assertEquals(output.keySet().toString(), stringGoalStateMap.keySet().toString());
        assertEquals(output.values().size(), stringGoalStateMap.values().size());
        assertEquals(
                output
                        .get(output.keySet().iterator().next())
                        .getPortStatesList()
                        .size(),
                stringGoalStateMap
                        .get(stringGoalStateMap.keySet().iterator().next())
                        .getPortStatesList()
                        .size());
    }

    private void L3Check_Second_Version(NetworkConfiguration input, Map<String, Goalstate.GoalState> output, Map<String, List<UTPortWithSubnetAndIPMapping>> existPortsMap) {

//        Map<String, Goalstate.GoalState> stringGoalStateMap =
//                goalStateManager.transformNorthToSouth(input);

        assertEquals(output.keySet().toString(), existPortsMap.keySet().toString());
        assertEquals(output.values().size(), existPortsMap.size());
        assertEquals(
                output
                        .get(output.keySet().iterator().next())
                        .getPortStatesList()
                        .size(),
                existPortsMap
                        .get(existPortsMap.keySet().iterator().next())
                        .size());
    }
}
