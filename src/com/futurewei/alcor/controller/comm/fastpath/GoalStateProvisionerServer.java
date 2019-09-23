package com.futurewei.alcor.controller.comm.fastpath;

import com.futurewei.alcor.controller.schema.*;
import com.futurewei.alcor.controller.service.Goalstateprovisioner;
import com.futurewei.alcor.controller.schema.*;
import com.futurewei.alcor.controller.schema.Goalstate.*;
import com.futurewei.alcor.controller.service.GoalStateProvisionerGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.logging.Logger;

public class GoalStateProvisionerServer {

    private static final Logger logger = Logger.getLogger(GoalStateProvisionerServer.class.getName());

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new GoalStateProvisionerImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
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
        public void pushNetworkResourceStates(GoalState state, StreamObserver<Goalstateprovisioner.GoalStateOperationReply> responseObserver){
            Goalstateprovisioner.GoalStateOperationReply.Builder reply = Goalstateprovisioner.GoalStateOperationReply.newBuilder();
            for (Vpc.VpcState vpcState : state.getVpcStatesList()){
                reply.addOperationStatuses(Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus.newBuilder()
                        .setResourceId(vpcState.getConfiguration().getId())
                        .setOperationStatusValue(Common.ResourceType.VPC_VALUE)
                        .setOperationType(vpcState.getOperationType())
                        .setOperationStatus(Common.OperationStatus.SUCCESS));
            }
            for (Subnet.SubnetState subnetState : state.getSubnetStatesList()){
                reply.addOperationStatuses(Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus.newBuilder()
                        .setResourceId(subnetState.getConfiguration().getId())
                        .setOperationStatusValue(Common.ResourceType.SUBNET_VALUE)
                        .setOperationType(subnetState.getOperationType())
                        .setOperationStatus(Common.OperationStatus.SUCCESS));
            }
            for (Port.PortState portState : state.getPortStatesList()){
                reply.addOperationStatuses(Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus.newBuilder()
                        .setResourceId(portState.getConfiguration().getId())
                        .setOperationStatusValue(Common.ResourceType.PORT_VALUE)
                        .setOperationType(portState.getOperationType())
                        .setOperationStatus(Common.OperationStatus.SUCCESS));
            }
            for (SecurityGroup.SecurityGroupState securityGroupState: state.getSecurityGroupStatesList()){
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
