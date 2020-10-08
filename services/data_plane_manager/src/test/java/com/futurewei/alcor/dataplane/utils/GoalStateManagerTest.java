package com.futurewei.alcor.dataplane.utils;

import com.futurewei.alcor.dataplane.config.UnitTestConfig;
import com.futurewei.alcor.dataplane.entity.UTPortWithSubnetAndIPMapping;
import com.futurewei.alcor.dataplane.entity.UTSubnetInfo;
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
        // configure Map<String(bindingHostIP), UTPortWithSubnetAndIPMapping>
        Map<String, UTPortWithSubnetAndIPMapping> map = new HashMap<>();
        UTPortWithSubnetAndIPMapping mapping = new UTPortWithSubnetAndIPMapping();
        List<PortEntity.FixedIp> fixedIps = new ArrayList<>();
        PortEntity.FixedIp fixedIp = new PortEntity.FixedIp("a87e0f87-a2d9-44ef-9194-9a62f1785940", "192.168.2.20");
        fixedIps.add(fixedIp);

        mapping.setBindingHostId("ephost_0");
        mapping.setFixedIps(fixedIps);
        mapping.setPortId("f37810eb-7f83-45fa-a4d4-1b31e75399d0");
        mapping.setPortMacAddress("86:ea:77:ad:52:50");
        mapping.setPortName("test_cni_port0");
        mapping.setVethName("veth0");
        map.put("10.213.43.187", mapping);

        // configure List<UTSubnetInfo>
        List<UTSubnetInfo> UTSubnets = new ArrayList<>();
        UTSubnetInfo utSubnetInfo = new UTSubnetInfo();
        utSubnetInfo.setSubnetCidr("192.168.2.0/24");
        utSubnetInfo.setSubnetGatewayIP("192.168.2.20");
        utSubnetInfo.setSubnetId("a87e0f87-a2d9-44ef-9194-9a62f1785940");
        utSubnetInfo.setSubnetName("test_subnet0");
        utSubnetInfo.setTunnelId(Long.parseLong("88888"));
        UTSubnets.add(utSubnetInfo);

        NetworkConfiguration input = util.autoGenerateUTsInput_MoreCustomizableScenarios(0, 2, map, UTSubnets, 2, 3, true, true, true, false, 0, true);
        Map<String, Goalstate.GoalState> output = util.autoGenerateUTsOutput_MoreCustomizableScenarios(0, 2, map, UTSubnets, 2, 3, true,true, true, false, 0, true);
        L3Check(input, output);
    }

    private void check(String input, String output) {

        NetworkConfiguration networkConfiguration = gson.fromJson(input, NetworkConfiguration.class);
        Map<String, Goalstate.GoalState> goalStateHashMap =
                new Gson().fromJson(output, new TypeToken<Map<String, Goalstate.GoalState>>() {
                }.getType());

        Map<String, Goalstate.GoalState> goalStateHashMap1 =
                gson.fromJson(output, new TypeToken<Map<String, Goalstate.GoalState>>() {
                }.getType());

        final Map<String, Goalstate.GoalState> stringGoalStateMap =
                goalStateManager.transformNorthToSouth(networkConfiguration);

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

        Map<String, Goalstate.GoalState> stringGoalStateMap =
                goalStateManager.transformNorthToSouth(input);

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
}
