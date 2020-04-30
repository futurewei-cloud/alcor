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

package com.futurewei.alcor.controller.comm.grpc;

import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
import com.futurewei.alcor.controller.schema.Goalstate.GoalState;
import com.futurewei.alcor.controller.service.GoalStateProvisionerGrpc;
import com.futurewei.alcor.controller.service.Goalstateprovisioner;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class GoalStateProvisionerClient {

    private final ManagedChannel channel;
    private final GoalStateProvisionerGrpc.GoalStateProvisionerBlockingStub blockingStub;
    private final GoalStateProvisionerGrpc.GoalStateProvisionerStub asyncStub;

    /**
     * Construct client connecting to GoalStateProvisioner server at {@code host:port}.
     */
    public GoalStateProvisionerClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build());
    }

    /**
     * Construct client for accessing GoalStateProvisioner server using the existing channel.
     */
    GoalStateProvisionerClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = GoalStateProvisionerGrpc.newBlockingStub(channel);
        asyncStub = GoalStateProvisionerGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void PushNetworkResourceStates(GoalState state) {
        Logger alcorLog = LoggerFactory.getLogger();
        alcorLog.entering(this.getClass().getName(), "PushNetworkResourceStates(GoalState state)");

        alcorLog.log(Level.INFO, "GoalStateProvisionerClient : Will try to send GS with fast path...");
        Goalstateprovisioner.GoalStateOperationReply response;
        try {
            response = blockingStub.pushNetworkResourceStates(state);
        } catch (StatusRuntimeException e) {
            alcorLog.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        alcorLog.log(Level.INFO, "Message total operation time: " + response.getMessageTotalOperationTime());
        alcorLog.log(Level.INFO, "Goal state operation status counts: " + response.getOperationStatusesCount());

        for (int i = 0; i < response.getOperationStatusesCount(); i++) {
            alcorLog.log(Level.INFO, "GS #" + i + ":" + response.getOperationStatuses(i));
        }
        alcorLog.exiting(this.getClass().getName(), "PushNetworkResourceStates(GoalState state)");

    }
}
