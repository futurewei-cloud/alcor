package com.futurewei.alioth.controller.comm.fastpath;

import com.futurewei.alioth.controller.schema.Goalstate.*;
import com.futurewei.alioth.controller.service.GoalStateProvisionerGrpc;
import com.futurewei.alioth.controller.service.Goalstateprovisioner.*;
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

    /** Construct client connecting to GoalStateProvisioner server at {@code host:port}. */
    public GoalStateProvisionerClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build());
    }

    /** Construct client for accessing GoalStateProvisioner server using the existing channel. */
    GoalStateProvisionerClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = GoalStateProvisionerGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void PushNetworkResourceStates(GoalState state) {
        logger.info("Will try to send GS with fast path...");
        GoalStateOperationReply response;
        try {
            response = blockingStub.pushNetworkResourceStates(state);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Goal state operation status counts: " + response.getOperationStatusesCount());
    }
}
