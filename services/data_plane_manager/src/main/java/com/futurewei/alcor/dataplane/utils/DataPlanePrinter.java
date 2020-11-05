/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
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