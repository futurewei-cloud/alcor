/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/


package com.futurewei.alcor.controller.comm.grpc;

import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
import com.futurewei.alcor.controller.schema.*;
import com.futurewei.alcor.controller.schema.Goalstate.GoalState;
import com.futurewei.alcor.controller.service.GoalStateProvisionerGrpc;
import com.futurewei.alcor.controller.service.Goalstateprovisioner;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.logging.Level;

public class GoalStateProvisionerServer {
    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new GoalStateProvisionerImpl())
                .build()
                .start();
        Logger logger = LoggerFactory.getLogger();
        logger.log(Level.INFO, "GoalStateProvisionerServer : Server started, listening on ");
        logger.log(Level.INFO, "Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                logger.log(Level.SEVERE, "*** shutting down gRPC server since JVM is shutting down");
                GoalStateProvisionerServer.this.stop();
                logger.log(Level.SEVERE, "*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    static class GoalStateProvisionerImpl extends GoalStateProvisionerGrpc.GoalStateProvisionerImplBase {

        @Override
        public void pushNetworkResourceStates(GoalState state, StreamObserver<Goalstateprovisioner.GoalStateOperationReply> responseObserver) {
            Goalstateprovisioner.GoalStateOperationReply.Builder reply = Goalstateprovisioner.GoalStateOperationReply.newBuilder();
            for (Vpc.VpcState vpcState : state.getVpcStatesList()) {
                reply.addOperationStatuses(Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus.newBuilder()
                        .setResourceId(vpcState.getConfiguration().getId())
                        .setOperationStatusValue(Common.ResourceType.VPC_VALUE)
                        .setOperationType(vpcState.getOperationType())
                        .setOperationStatus(Common.OperationStatus.SUCCESS));
            }
            for (Subnet.SubnetState subnetState : state.getSubnetStatesList()) {
                reply.addOperationStatuses(Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus.newBuilder()
                        .setResourceId(subnetState.getConfiguration().getId())
                        .setOperationStatusValue(Common.ResourceType.SUBNET_VALUE)
                        .setOperationType(subnetState.getOperationType())
                        .setOperationStatus(Common.OperationStatus.SUCCESS));
            }
            for (Port.PortState portState : state.getPortStatesList()) {
                reply.addOperationStatuses(Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus.newBuilder()
                        .setResourceId(portState.getConfiguration().getId())
                        .setOperationStatusValue(Common.ResourceType.PORT_VALUE)
                        .setOperationType(portState.getOperationType())
                        .setOperationStatus(Common.OperationStatus.SUCCESS));
            }
            for (SecurityGroup.SecurityGroupState securityGroupState : state.getSecurityGroupStatesList()) {
                reply.addOperationStatuses(Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus.newBuilder()
                        .setResourceId(securityGroupState.getConfiguration().getName())
                        .setOperationStatusValue(Common.ResourceType.SECURITYGROUP_VALUE)
                        .setOperationType(securityGroupState.getOperationType())
                        .setOperationStatus(Common.OperationStatus.SUCCESS));
            }
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
        }
    }
}
