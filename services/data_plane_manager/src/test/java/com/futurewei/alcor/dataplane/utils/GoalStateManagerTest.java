package com.futurewei.alcor.dataplane.utils;

import com.futurewei.alcor.dataplane.config.UnitTestConfig;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GoalStateManagerTest {

    static Gson gson = null;
    static GoalStateManager goalStateManager = null;

    @BeforeClass
    public static void setUp() {
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
     * unknown
     */
    @Test
    public void testFailure1DuringIntegrationWithNova() {
        String input = UnitTestConfig.testFailure1DuringIntegrationWithNova_input;
        String output = UnitTestConfig.testFailure1DuringIntegrationWithNova_output;
        check(input, output);
    }

    /**
     * unknown
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
     * unknown
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
}
