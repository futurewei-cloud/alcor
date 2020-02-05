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

import com.futurewei.alcor.controller.logging.Log;
import com.futurewei.alcor.controller.logging.LogFactory;
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
        Log alcorLog = LogFactory.getLog();
        System.out.println("GoalStateProvisionerServer : Server started, listening on ");
        alcorLog.log(Level.INFO, "Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GoalStateProvisionerServer.this.stop();
                System.err.println("*** server shut down");
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
