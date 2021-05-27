/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
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
     * Scenario: L3 - Create first Port (P1) without neighbor at Host 1
     */
    @Test
    public void scenario_L3_createFirstPortP1AtHost1_FastPathOnly() throws Exception {
        NetworkConfiguration input = util.autoGenerateUTsInput(0, 2, 1, 1, 1, 0, 0, true, true, true, false, 0, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput(0, 2, 1, 1, 1, 0, 0, true,true, true, false, 0, true);
        //L3Check(input, output);
    }

    /**
     * Scenario: L3 - Create first Port (P2) with neighbor P1 at Host 1
     */
    @Test
    public void scenario_L3_createFirstPortP2WithNeighborP1AtHost1_FastPathOnly() throws Exception {
        NetworkConfiguration input = util.autoGenerateUTsInput(0, 2, 1, 1, 1, 1, 1, false,false, false, true, 1, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput(0, 2, 1, 1, 1, 1, 1, false,false, false, true, 1, true);
        //L3Check(input, output);
    }

    /**
     * Scenario: L3 - Create Ports (P1, P2) without neighbor at Host 1, P1 and P2 are associated with different subnet
     */
    @Test
    public void scenario_L3_createPortP1P2WithoutNeighborAtHost1_FastPathOnly() throws Exception {
        NetworkConfiguration input = util.autoGenerateUTsInput(0, 2, 2, 1, 2, 0, 0, false,false, false, false, 0, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput(0, 2, 2, 1, 2, 0, 0, false,false, false, false, 0, true);
        //L3Check(input, output);
    }

    /**
     * Scenario: L3 - Create Ports (P2, P3) with neighbor P1 at Host 1, P2 and P3 are associated with different subnet
     */
    @Test
    public void scenario_L3_createPortP2P3WithNeighborP1AtHost1_FastPathOnly() throws Exception {
        NetworkConfiguration input = util.autoGenerateUTsInput(0, 2, 2, 1, 2, 2, 0, false,false, false, true, 1, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput(0, 2, 2, 1, 2, 2, 0, false,false, false, true, 1, true);
        //L3Check(input, output);
    }

    /**
     * Scenario: L3 - Create Port (P1) without neighbor at Host 1 and Port (P2) without neighbor at Host 2, P1 and P2 are associated with same subnet
     */
    @Test
    public void scenario_L3_createPortP1P2WithoutNeighborAtHost1AndHost2_FastPathOnly() throws Exception {
        NetworkConfiguration input = util.autoGenerateUTsInput(0, 2, 1, 2, 2, 2, 0, false,false, false, false, 0, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput(0, 2, 1, 2, 2, 2, 0, false,false, false, false, 0, true);
        //L3Check(input, output);
    }

    /**
     * Scenario: L3 - Create Port (P1) with neighbor at Host 1 and Port (P2) with neighbor at Host 2, P1 and P2 are associated with same subnet
     */
    @Test
    public void scenario_L3_createPortP1P2WithNeighborAtHost1AndHost2_FastPathOnly() throws Exception {
        NetworkConfiguration input = util.autoGenerateUTsInput(0, 2, 1, 2, 2, 2, 0, false,false, false, true, 1, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput(0, 2, 1, 2, 2, 2, 0, false,false, false, true, 1, true);
        //L3Check(input, output);
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
        PortEntity.FixedIp fixedIp = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785940", "192.168.2.2");
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
        //existPortsMap.put("10.213.43.187", mapList);

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

        // operationType = Create
        NetworkConfiguration input = util.autoGenerateUTsInput_MoreCustomizableScenarios(0, 2, createPortsMap, UTSubnets, L3NeighborInfoMapping, true, true, true, false, neighborInfoDetails, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput_MoreCustomizableScenarios(0, 2, createPortsMap, existPortsMap, UTSubnets, L3NeighborInfoMapping, true,true, true, false, neighborInfoDetails, true);
        //L3Check_Second_Version(input, output, existPortsMap);

        // operationType = Update
        NetworkConfiguration updateInput = util.autoGenerateUTsInput_MoreCustomizableScenarios(0, 2, createPortsMap, UTSubnets, L3NeighborInfoMapping, true, true, true, false, neighborInfoDetails, true);
        Map<String, Goalstate.GoalState> updateOutput = util.autoGenerateUTsOutput_MoreCustomizableScenarios(0, 2, createPortsMap, existPortsMap, UTSubnets, L3NeighborInfoMapping, true,true, true, false, neighborInfoDetails, true);
        //L3Check_Second_Version(updateInput, updateOutput, existPortsMap);
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
        PortEntity.FixedIp fixedIp1 = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785940", "192.168.2.2");
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
        PortEntity.FixedIp fixedIp2 = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785940", "192.168.2.3");
        fixedIps2.add(fixedIp2);

        mapping2.setBindingHostId("ephost_0");
        mapping2.setFixedIps(fixedIps2);
        mapping2.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d1");
        mapping2.setPortMacAddress("86:ea:77:ad:52:51");
        mapping2.setPortName("test_cni_port2");
        mapping2.setVethName("veth0");

        mapList.add(mapping2);
        //mapList.add(mapping1);

        createPortsMap.put("10.213.43.187", mapList);

        // configure Map<String(bindingHostIP), UTPortWithSubnetAndIPMapping> existPortsMap
        Map<String, List<UTPortWithSubnetAndIPMapping>> existPortsMap = new HashMap<>();
        List<UTPortWithSubnetAndIPMapping> existPortsMapList = new ArrayList<>();
        existPortsMap.put("10.213.43.187", existPortsMapList);
        //existPortsMap.put("10.213.43.187", mapList);

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
//        UTL3NeighborInfoMapping utl3NeighborInfoMapping = new UTL3NeighborInfoMapping();
//        utl3NeighborInfoMapping.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785940");
//        utl3NeighborInfoMapping.setIPsInSubnet(new ArrayList<>(){{add(new UTIPInfo("192.168.2.20", false));add(new UTIPInfo("192.168.2.21", false));}});
//        L3NeighborInfoMapping.add(utl3NeighborInfoMapping);

        // configure List<UTNeighborInfoDetail>
        Map<String, UTNeighborInfoDetail> neighborInfoDetails = new HashMap<>();


        // operationType = Create
        NetworkConfiguration input = util.autoGenerateUTsInput_MoreCustomizableScenarios(0, 2, createPortsMap, UTSubnets, L3NeighborInfoMapping, true, true, true, false, neighborInfoDetails, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput_MoreCustomizableScenarios(0, 2, createPortsMap, existPortsMap, UTSubnets, L3NeighborInfoMapping, true,true, false, false, neighborInfoDetails, true);
        //L3Check_Second_Version(input, output, existPortsMap);

        // operationType = Update
        NetworkConfiguration updateInput = util.autoGenerateUTsInput_MoreCustomizableScenarios(1, 2, createPortsMap, UTSubnets, L3NeighborInfoMapping, true, true, true, false, neighborInfoDetails, true);
        Map<String, Goalstate.GoalState> updateOutput = util.autoGenerateUTsOutput_MoreCustomizableScenarios(1, 2, createPortsMap, existPortsMap, UTSubnets, L3NeighborInfoMapping, true,true, false, false, neighborInfoDetails, true);
        //L3Check_Second_Version(updateInput, updateOutput, existPortsMap);
    }

    /**
     * cenario: L3_Customize_Second_Version -
     * Step 1 : create port P1(d0) - 2.1 without neighbor at host1
     * Step 2 : then create port P2(d1) - 2.2 at host1
     * Step 3 : then create port P3(d2) - 3.3 at host2
     * @throws Exception
     */
    @Test
    public void scenario_L3_Customize_Second_Version_createPortP1WithoutNeighborAtHost1_andThen_createPortP2AtHost1_andThen_createPortP3AtHost2_FastPathOnly() throws Exception {
        // Step 1
        // configure Map<String(bindingHostIP), UTPortWithSubnetAndIPMapping> createPortsMap
        Map<String, List<UTPortWithSubnetAndIPMapping>> createPortsMap = new HashMap<>();
        List<UTPortWithSubnetAndIPMapping> mapList = new ArrayList<>();
        UTPortWithSubnetAndIPMapping mapping = new UTPortWithSubnetAndIPMapping();
        List<PortEntity.FixedIp> fixedIps = new ArrayList<>();
        PortEntity.FixedIp fixedIp = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785940", "192.168.2.2");
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
        utl3NeighborInfoMapping.setIPsInSubnet(new ArrayList<>(){{add(new UTIPInfo("192.168.2.1", false));}});
        L3NeighborInfoMapping.add(utl3NeighborInfoMapping);

        // configure List<UTNeighborInfoDetail>
        Map<String, UTNeighborInfoDetail> neighborInfoDetails = new HashMap<>();

        // operationType = Create
        NetworkConfiguration input = util.autoGenerateUTsInput_MoreCustomizableScenarios(0, 2, createPortsMap, UTSubnets, L3NeighborInfoMapping, true, true, true, false, neighborInfoDetails, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput_MoreCustomizableScenarios(0, 2, createPortsMap, existPortsMap, UTSubnets, L3NeighborInfoMapping, true,true, true, false, neighborInfoDetails, true);
        //L3Check_Second_Version(input, output, existPortsMap);

        // Step 2
        // configure Map<String(bindingHostIP), UTPortWithSubnetAndIPMapping> createPortsMap
        Map<String, List<UTPortWithSubnetAndIPMapping>> createPortsMap2 = new HashMap<>();
        List<UTPortWithSubnetAndIPMapping> mapList2 = new ArrayList<>();
        UTPortWithSubnetAndIPMapping mapping2 = new UTPortWithSubnetAndIPMapping();
        List<PortEntity.FixedIp> fixedIps2 = new ArrayList<>();
        PortEntity.FixedIp fixedIp2 = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785940", "192.168.2.2");
        fixedIps2.add(fixedIp2);

        mapping2.setBindingHostId("ephost_0");
        mapping2.setFixedIps(fixedIps2);
        mapping2.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d1");
        mapping2.setPortMacAddress("86:ea:77:ad:52:50");
        mapping2.setPortName("test_cni_port2");
        mapping2.setVethName("veth0");

        mapList2.add(mapping2);
        createPortsMap2.put("10.213.43.187", mapList2);

        // configure Map<String(bindingHostIP), UTPortWithSubnetAndIPMapping> existPortsMap
        Map<String, List<UTPortWithSubnetAndIPMapping>> existPortsMap2 = new HashMap<>();
        List<UTPortWithSubnetAndIPMapping> existPortsMapList2 = new ArrayList<>();

        // P1 configuration
        UTPortWithSubnetAndIPMapping existMapping1 = new UTPortWithSubnetAndIPMapping();
        List<UTPortWithSubnetAndIPMapping> existPortsMapList1 = new ArrayList<>();
        List<PortEntity.FixedIp> existFixedIps1 = new ArrayList<>();
        PortEntity.FixedIp existFixedIp1 = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785940", "192.168.2.1");
        existFixedIps1.add(existFixedIp1);

        existMapping1.setBindingHostId("ephost_0");
        existMapping1.setFixedIps(existFixedIps1);
        existMapping1.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d0");
        existMapping1.setPortMacAddress("86:ea:77:ad:52:55");
        existMapping1.setPortName("test_cni_port1");
        existMapping1.setVethName("veth0");
        existPortsMapList1.add(existMapping1);

        existPortsMap2.put("10.213.43.187", existPortsMapList2);

        // configure List<UTSubnetInfo>
        List<UTSubnetInfo> UTSubnets2 = new ArrayList<>();
        UTSubnetInfo utSubnetInfo2 = new UTSubnetInfo();
        utSubnetInfo2.setSubnetCidr("192.168.2.0/24");
        utSubnetInfo2.setSubnetGatewayIP("192.168.2.20");
        utSubnetInfo2.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785940");
        utSubnetInfo2.setSubnetName("test_subnet0");
        utSubnetInfo2.setTunnelId(Long.parseLong("88888"));
        UTSubnets2.add(utSubnetInfo2);

        // configure List<UTL3NeighborInfoMapping>
        List<UTL3NeighborInfoMapping> L3NeighborInfoMapping2 = new ArrayList<>();
        UTL3NeighborInfoMapping utl3NeighborInfoMapping1 = new UTL3NeighborInfoMapping();
        utl3NeighborInfoMapping1.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785940");
        utl3NeighborInfoMapping1.setIPsInSubnet(new ArrayList<>(){{add(new UTIPInfo("192.168.2.1", true));
            add(new UTIPInfo("192.168.2.2", false));}});
        L3NeighborInfoMapping2.add(utl3NeighborInfoMapping1);

        // configure List<UTNeighborInfoDetail>
        Map<String, UTNeighborInfoDetail> neighborInfoDetails2 = new HashMap<>();
        UTNeighborInfoDetail detail1 = new UTNeighborInfoDetail("f37810eb-7f83-45fa-a4d4-1b31e75399d0",
                "86:ea:77:ad:52:55",
                "ephost_0",
                "10.213.43.187");
        neighborInfoDetails2.put( "192.168.2.1", detail1);

        // operationType = Create
        NetworkConfiguration input2 = util.autoGenerateUTsInput_MoreCustomizableScenarios(0, 2, createPortsMap2, UTSubnets2, L3NeighborInfoMapping2, true, true, true, false, neighborInfoDetails2, true);
        Map<String, Goalstate.GoalState> output2 = util.autoGenerateUTsOutput_MoreCustomizableScenarios(0, 2, createPortsMap2, existPortsMap2, UTSubnets2, L3NeighborInfoMapping2, true,true, true, false, neighborInfoDetails2, true);
        //L3Check_Second_Version(input, output, existPortsMap);

        // Step 3
        // configure Map<String(bindingHostIP), UTPortWithSubnetAndIPMapping>
        Map<String, List<UTPortWithSubnetAndIPMapping>> createPortsMap3 = new HashMap<>();
        List<UTPortWithSubnetAndIPMapping> mapList3 = new ArrayList<>();

        // P3 configuration
        UTPortWithSubnetAndIPMapping mapping1 = new UTPortWithSubnetAndIPMapping();
        List<PortEntity.FixedIp> fixedIps1 = new ArrayList<>();
        PortEntity.FixedIp fixedIp1 = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785941", "192.168.3.3");
        fixedIps1.add(fixedIp1);

        mapping1.setBindingHostId("ephost_1");
        mapping1.setFixedIps(fixedIps1);
        mapping1.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d2");
        mapping1.setPortMacAddress("86:ea:77:ad:52:50");
        mapping1.setPortName("test_cni_port3");
        mapping1.setVethName("veth0");

        mapList3.add(mapping1);

        createPortsMap3.put("10.213.43.187", mapList3);
        createPortsMap3.put("10.213.43.163", new ArrayList<>());

        // configure Map<String(bindingHostIP), UTPortWithSubnetAndIPMapping> existPortsMap
        Map<String, List<UTPortWithSubnetAndIPMapping>> existPortsMap3 = new HashMap<>();
        List<UTPortWithSubnetAndIPMapping> existPortsMapList13 = new ArrayList<>();
        List<UTPortWithSubnetAndIPMapping> existPortsMapList23 = new ArrayList<>();

        // P1 configuration
        UTPortWithSubnetAndIPMapping existMapping13 = new UTPortWithSubnetAndIPMapping();
        List<PortEntity.FixedIp> existFixedIps13 = new ArrayList<>();
        PortEntity.FixedIp existFixedIp13 = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785940", "192.168.2.1");
        existFixedIps13.add(existFixedIp13);

        existMapping13.setBindingHostId("ephost_0");
        existMapping13.setFixedIps(existFixedIps13);
        existMapping13.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d0");
        existMapping13.setPortMacAddress("86:ea:77:ad:52:55");
        existMapping13.setPortName("test_cni_port1");
        existMapping13.setVethName("veth0");
        existPortsMapList13.add(existMapping13);

        // P2 configuration
        UTPortWithSubnetAndIPMapping existMapping2 = new UTPortWithSubnetAndIPMapping();
        List<PortEntity.FixedIp> existFixedIps2 = new ArrayList<>();
        PortEntity.FixedIp existFixedIp2 = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785940", "192.168.2.2");
        existFixedIps2.add(existFixedIp2);

        existMapping2.setBindingHostId("ephost_0");
        existMapping2.setFixedIps(existFixedIps2);
        existMapping2.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d1");
        existMapping2.setPortMacAddress("86:ea:77:ad:52:55");
        existMapping2.setPortName("test_cni_port2");
        existMapping2.setVethName("veth0");
        existPortsMapList13.add(existMapping2);


        existPortsMap3.put("10.213.43.187", existPortsMapList13);
        //existPortsMap.put("10.213.43.187", mapList);
        existPortsMap3.put("10.213.43.163", existPortsMapList23);

        // configure List<UTSubnetInfo>
        List<UTSubnetInfo> UTSubnets3 = new ArrayList<>();
        UTSubnetInfo utSubnetInfo1 = new UTSubnetInfo();
        utSubnetInfo1.setSubnetCidr("192.168.2.0/24");
        utSubnetInfo1.setSubnetGatewayIP("192.168.2.20");
        utSubnetInfo1.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785940");
        utSubnetInfo1.setSubnetName("test_subnet0");
        utSubnetInfo1.setTunnelId(Long.parseLong("88888"));
        UTSubnets3.add(utSubnetInfo1);

        UTSubnetInfo utSubnetInfo23 = new UTSubnetInfo();
        utSubnetInfo23.setSubnetCidr("192.168.3.0/24");
        utSubnetInfo23.setSubnetGatewayIP("192.168.3.20");
        utSubnetInfo23.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785941");
        utSubnetInfo23.setSubnetName("test_subnet1");
        utSubnetInfo23.setTunnelId(Long.parseLong("88888"));
        UTSubnets3.add(utSubnetInfo23);

        // configure List<UTL3NeighborInfoMapping>
        List<UTL3NeighborInfoMapping> L3NeighborInfoMapping3 = new ArrayList<>();
        UTL3NeighborInfoMapping utl3NeighborInfoMapping13 = new UTL3NeighborInfoMapping();
        utl3NeighborInfoMapping13.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785940");
        utl3NeighborInfoMapping13.setIPsInSubnet(new ArrayList<>(){{add(new UTIPInfo("192.168.2.1", true));
            add(new UTIPInfo("192.168.2.2", true));}});
        L3NeighborInfoMapping3.add(utl3NeighborInfoMapping13);

        UTL3NeighborInfoMapping utl3NeighborInfoMapping2 = new UTL3NeighborInfoMapping();
        utl3NeighborInfoMapping2.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785941");
        utl3NeighborInfoMapping2.setIPsInSubnet(new ArrayList<>(){{add(new UTIPInfo("192.168.3.3", false));}});
        L3NeighborInfoMapping3.add(utl3NeighborInfoMapping2);

        // configure Map<String, UTNeighborInfoDetail>, key - port_IP
        Map<String, UTNeighborInfoDetail> neighborInfoDetails3 = new HashMap<>();

        UTNeighborInfoDetail detail13 = new UTNeighborInfoDetail("f37810eb-7f83-45fa-a4d4-1b31e75399d0",
                "86:ea:77:ad:52:55",
                "ephost_0",
                "10.213.43.187");
        neighborInfoDetails3.put( "192.168.2.1", detail13);
        UTNeighborInfoDetail detail2 = new UTNeighborInfoDetail("f37810eb-7f83-45fa-a4d4-1b31e75399d1",
                "86:ea:77:ad:52:57",
                "ephost_0",
                "10.213.43.187");
        neighborInfoDetails3.put( "192.168.2.2", detail2);


        NetworkConfiguration input3 = util.autoGenerateUTsInput_MoreCustomizableScenarios(0, 2, createPortsMap3, UTSubnets3, L3NeighborInfoMapping3, true, true, true, true, neighborInfoDetails3, true);
        Map<String, Goalstate.GoalState> output3 = util.autoGenerateUTsOutput_MoreCustomizableScenarios(0, 2, createPortsMap3, existPortsMap3, UTSubnets3, L3NeighborInfoMapping3, true,true, true, true, neighborInfoDetails3, true);
        //L3Check_Second_Version(input3, output3, existPortsMap);
    }

    /**
     * Scenario: L3_Customize_Second_Version - Create Port P6 with neighbor at Host 1,
     * Exist port : P1(d0) - 2.2, P2(d1) - 3.4, P3(d2) - 2.3,3.3, P4(d3) - 3.2
     * (P1, P2) are in Host 1, (P3, P4) are in Host 2
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
        createPortsMap.put("10.213.43.163", new ArrayList<>());

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
        existMapping2.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d1");
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
        existMapping4.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d3");
        existMapping4.setPortMacAddress("86:ea:77:ad:52:58");
        existMapping4.setPortName("test_cni_port4");
        existMapping4.setVethName("veth0");
        existPortsMapList2.add(existMapping4);

        existPortsMap.put("10.213.43.187", existPortsMapList1);
        //existPortsMap.put("10.213.43.187", mapList);
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
        utSubnetInfo2.setSubnetCidr("192.168.3.0/24");
        utSubnetInfo2.setSubnetGatewayIP("192.168.3.20");
        utSubnetInfo2.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785941");
        utSubnetInfo2.setSubnetName("test_subnet1");
        utSubnetInfo2.setTunnelId(Long.parseLong("88888"));
        UTSubnets.add(utSubnetInfo2);

        // configure List<UTL3NeighborInfoMapping>
        List<UTL3NeighborInfoMapping> L3NeighborInfoMapping = new ArrayList<>();
        UTL3NeighborInfoMapping utl3NeighborInfoMapping1 = new UTL3NeighborInfoMapping();
        utl3NeighborInfoMapping1.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785940");
        utl3NeighborInfoMapping1.setIPsInSubnet(new ArrayList<>(){{add(new UTIPInfo("192.168.2.2", true));
            add(new UTIPInfo("192.168.2.3", true));}});
        L3NeighborInfoMapping.add(utl3NeighborInfoMapping1);

        UTL3NeighborInfoMapping utl3NeighborInfoMapping2 = new UTL3NeighborInfoMapping();
        utl3NeighborInfoMapping2.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785941");
        utl3NeighborInfoMapping2.setIPsInSubnet(new ArrayList<>(){{add(new UTIPInfo("192.168.3.4", true));
            add(new UTIPInfo("192.168.3.2", true));add(new UTIPInfo("192.168.3.3", true)); add(new UTIPInfo("192.168.3.5", false));}});
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
        assertEquals(
                output
                        .get(output.keySet().iterator().next()),
                stringGoalStateMap
                        .get(stringGoalStateMap.keySet().iterator().next()));
    }

    private void L3Check_Second_Version(NetworkConfiguration input, Map<String, Goalstate.GoalState> output, Map<String, List<UTPortWithSubnetAndIPMapping>> existPortsMap) throws ExecutionException, InterruptedException {

        Map<String, Goalstate.GoalState> stringGoalStateMap =
                goalStateManager.transformNorthToSouth(input).get();

//        assertEquals(output.keySet().toString(), stringGoalStateMap.keySet().toString());
//        assertEquals(output.values().size(), stringGoalStateMap.values().size());
//        assertEquals(
//                output
//                        .get(output.keySet().iterator().next())
//                        .getPortStatesList()
//                        .size(),
//                stringGoalStateMap
//                        .get(stringGoalStateMap.keySet().iterator().next())
//                        .getPortStatesList()
//                        .size());

        for (Map.Entry<String, Goalstate.GoalState> outputEntry : output.entrySet()) {
            String outputKey = outputEntry.getKey();
            Goalstate.GoalState outputValue = outputEntry.getValue();
            Goalstate.GoalState dpmOutput = stringGoalStateMap.get(outputKey);
            assertEquals(outputValue, dpmOutput);
        }
    }
}
