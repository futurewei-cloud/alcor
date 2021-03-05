package com.futurewei.alcor.netwconfigmanager.server.grpc;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.netwconfigmanager.server.NetworkConfigServer;
import com.futurewei.alcor.schema.*;
import io.grpc.stub.StreamObserver;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.logging.Level;

public class GoalStateProvisionerServer implements NetworkConfigServer {

    private static final Logger logger = LoggerFactory.getLogger();
    private Server server;

    @Override
    public void start(int port) throws IOException {
        /* The port on which the server should run */
        server = ServerBuilder.forPort(port)
                .addService(new GoalStateProvisionerImpl())
                .build()
                .start();
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

    @Override
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    @Override
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
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
    }
}
