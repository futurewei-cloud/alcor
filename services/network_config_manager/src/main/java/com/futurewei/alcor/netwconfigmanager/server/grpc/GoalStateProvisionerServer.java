package com.futurewei.alcor.netwconfigmanager.server.grpc;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.netwconfigmanager.server.NetworkConfigServer;
import com.futurewei.alcor.schema.*;
import io.grpc.stub.StreamObserver;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class GoalStateProvisionerServer implements NetworkConfigServer {

    private static final Logger logger = LoggerFactory.getLogger();

    private final int port;
    private final Server server;

    public GoalStateProvisionerServer(int port){
        this.port = port;
        this.server = ServerBuilder.forPort(port)
                .addService(new GoalStateProvisionerImpl())
                .build();
    }

    @Override
    public void start(int port) throws IOException {
        this.server.start();
        logger.log(Level.INFO, "GoalStateProvisionerServer : Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                logger.log(Level.INFO, "*** shutting down gRPC server since JVM is shutting down");
                try {
                    GoalStateProvisionerServer.this.stop();
                } catch (InterruptedException e){
                    logger.log(Level.WARNING, "*** gRPC server shut down error");
                }
                logger.log(Level.INFO, "*** server shut down");
            }
        });
    }

    @Override
    public void stop() throws InterruptedException{
        if (this.server != null) {
            this.server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    @Override
    public void blockUntilShutdown() throws InterruptedException {
        if (this.server != null) {
            this.server.awaitTermination();
        }
    }

    static class GoalStateProvisionerImpl extends GoalStateProvisionerGrpc.GoalStateProvisionerImplBase {

        @Override
        public void pushNetworkResourceStates(Goalstate.GoalState state, StreamObserver<Goalstateprovisioner.GoalStateOperationReply> responseObserver) {
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

        @Override
        public StreamObserver<Goalstate.GoalStateV2> pushGoalStatesStream(final StreamObserver<Goalstateprovisioner.GoalStateOperationReply> responseObserver){
            return new StreamObserver<Goalstate.GoalStateV2>() {
                @Override
                public void onNext(Goalstate.GoalStateV2 value) {
                    //group resource based on host id
                    //store the goal state in cache
                    //prepare GS message based on host
                    Map<String, Goalstate.HostResources> hostResources = value.getHostResourcesMap();
                    for (String hostId : hostResources.keySet()) {
                        Goalstate.HostResources resources = hostResources.get(hostId);
                    }

                    //send them down to target ACA

                    //consolidate response from ACA and send response to DPM

                }

                @Override
                public void onError(Throwable t) {
                    logger.log(Level.WARNING, "*** pushGoalStatesStream cancelled");
                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
