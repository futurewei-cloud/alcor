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
package com.futurewei.alcor.dataplane.client.grpc;

import com.futurewei.alcor.dataplane.client.DataPlaneClient;
import com.futurewei.alcor.dataplane.config.grpc.GoalStateProvisionerClient;
import com.futurewei.alcor.dataplane.entity.HostGoalState;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class DataPlaneClientImpl implements DataPlaneClient {

    private int grpcPort;

    private static ExecutorService executor = new ThreadPoolExecutor(100, 200, 50, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(),
            new DefaultThreadFactory("grpc send pool"));

    @Autowired
    public DataPlaneClientImpl(@Value("${dataplane.grpc.port}")int grpcPort) {
        this.grpcPort = grpcPort;
    }

    @Override
    public Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>
    createGoalState(Goalstate.GoalState goalState, String hostIp) throws Exception {
        return sendGoalState(goalState, hostIp);
    }

    @Override
    public List<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>>
    createGoalState(List<HostGoalState> hostGoalStates) throws Exception {
        return sendGoalState(hostGoalStates);
    }

    @Override
    public List<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>>
    updateGoalState(List<HostGoalState> hostGoalStates) throws Exception {
        return sendGoalState(hostGoalStates);
    }

    @Override
    public List<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>>
    deleteGoalState(List<HostGoalState> hostGoalStates) throws Exception {
        return sendGoalState(hostGoalStates);
    }

    private List<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>>
    sendGoalState(List<HostGoalState> hostGoalStates) throws Exception {
        List<Future<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>>>
                futures = new ArrayList<>(hostGoalStates.size());
        for (HostGoalState hostGoalState: hostGoalStates) {
            Future<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>> future =
                    executor.submit(() -> {
                try {
                    return sendGoalState(hostGoalState);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            });
            futures.add(future);
        }
        return futures.parallelStream().map(future -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
    }

    private Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>
    sendGoalState(HostGoalState hostGoalState) throws InterruptedException {
        return sendGoalState(hostGoalState.getGoalState(), hostGoalState.getHostIp());
    }

    private Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>
    sendGoalState(Goalstate.GoalState goalState, String hostIp) throws InterruptedException {
        GoalStateProvisionerClient goalStateProvisionerClient =
                new GoalStateProvisionerClient(hostIp, grpcPort);
        List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus> statuses =
                goalStateProvisionerClient.PushNetworkResourceStates(goalState);
        goalStateProvisionerClient.shutdown();
        Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>> result = new HashMap<>();
        result.put(hostIp, statuses);
        return result;
    }
}
