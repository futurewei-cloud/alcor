/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.futurewei.alcor.dataplane.config.grpc;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.dataplane.config.Config;
import com.futurewei.alcor.schema.GoalStateProvisionerGrpc;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.List;
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
        channel.shutdown().awaitTermination(Config.SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
    }

    public List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>
    PushNetworkResourceStates(Goalstate.GoalState state) {
        Logger alcorLog = LoggerFactory.getLogger();
        alcorLog.entering(this.getClass().getName(), "PushNetworkResourceStates(GoalState state)");

        alcorLog.log(Level.INFO, "GoalStateProvisionerClient : Will try to send GS with fast path...");
        Goalstateprovisioner.GoalStateOperationReply response=null;
        try {
            response = blockingStub.pushNetworkResourceStates(state);
        } catch (StatusRuntimeException e) {
            alcorLog.log(Level.WARNING,"############ ex-response is "+response);
            e.printStackTrace();
            alcorLog.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return response.getOperationStatusesList();
        }
        alcorLog.log(Level.INFO, "Message total operation time: " + response.getMessageTotalOperationTime());
        alcorLog.log(Level.INFO, "Goal state operation status counts: " + response.getOperationStatusesCount());

        for (int i = 0; i < response.getOperationStatusesCount(); i++) {
            alcorLog.log(Level.INFO, "GS #" + i + ":" + response.getOperationStatuses(i));
        }
        alcorLog.exiting(this.getClass().getName(), "PushNetworkResourceStates(GoalState state)");
        return response.getOperationStatusesList();

    }
}
