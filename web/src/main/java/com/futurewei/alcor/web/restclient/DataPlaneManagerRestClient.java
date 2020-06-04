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
package com.futurewei.alcor.web.restclient;

import com.futurewei.alcor.schema.Goalstate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;

public class DataPlaneManagerRestClient extends AbstractRestClient {
    @Value("${microservices.dataplane.service.url:#{\"\"}}")
    private String dataPlaneManagerUrl;

    public void createGoalState(Goalstate.GoalState goalState) throws Exception {
        HttpEntity<Goalstate.GoalState> request = new HttpEntity<>(goalState);
        restTemplate.postForObject(dataPlaneManagerUrl, request, String[].class);
    }

    public void deleteGoalState(Goalstate.GoalState goalState) throws Exception {
        //FIXME: Not support yet
    }

    public void updateGoalState(Goalstate.GoalState goalState) throws Exception {
        HttpEntity<Goalstate.GoalState> request = new HttpEntity<>(goalState);
        restTemplate.put(dataPlaneManagerUrl, request, String[].class);
    }
}
