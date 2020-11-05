package com.futurewei.alcor.dataplane.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.web.entity.dataplane.NetworkConfiguration;
import java.util.logging.*;


public class DataPlanePrinter {
    public DataPlanePrinter() {
    }

    /**
     * print dpm input msg
     *
     * @param networkConfiguration msg
     */
    public void printNetworkConfiguration(NetworkConfiguration networkConfiguration) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(networkConfiguration);
            GoalStateManager.LOG.log(Level.INFO, "@@@input json str: \n" );
            GoalStateManager.LOG.log(Level.INFO, json);

        } catch (JsonProcessingException e) {
            GoalStateManager.LOG.log(Level.SEVERE, e.getMessage());
        }
    }
}