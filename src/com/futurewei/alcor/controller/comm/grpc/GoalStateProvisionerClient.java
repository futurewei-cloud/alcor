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

import com.futurewei.alcor.controller.service.Goalstateprovisioner;
import com.futurewei.alcor.controller.schema.Goalstate.*;
import com.futurewei.alcor.controller.service.GoalStateProvisionerGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoalStateProvisionerClient {

    private static final Logger logger = Logger.getLogger(GoalStateProvisionerClient.class.getName());

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
        logger.info("Will try to send GS with fast path...");
        Goalstateprovisioner.GoalStateOperationReply response;
        try {
            response = blockingStub.pushNetworkResourceStates(state);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Message total operation time: " + response.getMessageTotalOperationTime());
        logger.info("Goal state operation status counts: " + response.getOperationStatusesCount());

        for (int i = 0; i < response.getOperationStatusesCount(); i++) {
            logger.info("GS #" + i + ":" + response.getOperationStatuses(i));
        }
    }
}
