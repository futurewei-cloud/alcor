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
package com.futurewei.alcor.dataplane.service.impl;

import com.futurewei.alcor.common.message.MessageClient;
import com.futurewei.alcor.dataplane.config.Config;
import com.futurewei.alcor.dataplane.config.grpc.GoalStateProvisionerClient;
import com.futurewei.alcor.dataplane.service.GoalStateService;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix = "grpc")
public class OVSGoalStateServiceImpl implements GoalStateService {
    public static int getPort() {
        return port;
    }

    public static void setPort(int port) {
        OVSGoalStateServiceImpl.port = port;
    }

    public static int port ;

    public Goalstate.GoalState getGoalState() {
        return goalState;
    }

    public void setGoalState(Goalstate.GoalState goalState) {
        this.goalState = goalState;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isFastPath() {
        return isFastPath;
    }

    public void setFastPath(boolean fastPath) {
        isFastPath = fastPath;
    }

    private Goalstate.GoalState goalState;
    private String ip;
    private boolean isFastPath = false;

    public GoalStateProvisionerClient getGRpcClientForEpHost() {
        return gRpcClientForEpHost;
    }

    public void setGRpcClientForEpHost(GoalStateProvisionerClient gRpcClientForEpHost) {
        this.gRpcClientForEpHost = gRpcClientForEpHost;
    }

    public MessageClient getKafkaClient() {
        return kafkaClient;
    }

    public void setKafkaClient(MessageClient kafkaClient) {
        this.kafkaClient = kafkaClient;
    }

    GoalStateProvisionerClient gRpcClientForEpHost = null;
    MessageClient kafkaClient = null;

    public OVSGoalStateServiceImpl() {
    }

    public OVSGoalStateServiceImpl(String ip, Goalstate.GoalState gs,
                                   boolean isFastPath) {
        this.goalState = gs;
        this.ip = ip;
        this.isFastPath = isFastPath;
    }

    @Override
    public Goalstateprovisioner.GoalStateOperationReply SendGoalStateToHosts() {

        Goalstateprovisioner.GoalStateOperationReply r = null;

        if (isFastPath) {
            new GoalStateProvisionerClient(ip, port);
            this.setGRpcClientForEpHost(new GoalStateProvisionerClient(ip, port));
            r = new GoalStateProvisionerClient(ip, port).PushNetworkResourceStates(goalState);
        } else {
            String topicForEndpoint = Config.PRODUCER_CLIENT_ID + ip;
            this.getKafkaClient().runProducer(topicForEndpoint, goalState);

        }
        return r;

    }
}
