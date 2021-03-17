package com.futurewei.alcor.netwconfigmanager.server.grpc;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.netwconfigmanager.client.GoalStateClient;
import com.futurewei.alcor.netwconfigmanager.client.gRPC.GoalStateClientImpl;
import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.netwconfigmanager.server.NetworkConfigServer;
import com.futurewei.alcor.netwconfigmanager.util.NetworkConfigManagerUtil;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.GoalStateProvisionerGrpc;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Goalstateprovisioner.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

@Service
public class OnDemandServer implements NetworkConfigServer {


    private static final Logger logger = LoggerFactory.getLogger();

    private final int port;
    private final Server server;

//    @Autowired
//    private GoalStateClient grpcGoalStateClient;

    public OnDemandServer() {
        this.port = 9017;
        this.server = ServerBuilder.forPort(this.port)
                .addService(new OnDemandServerImpl())
                .build();
    }

    /**
     * Start a server with given port
     */
    @Override
    public void start() throws IOException {
        this.server.start();
        logger.log(Level.INFO, "OnDemandServer : Server started, listening on " + this.port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                logger.log(Level.INFO, "*** shutting down gRPC server since JVM is shutting down");
                try {
                    OnDemandServer.this.stop();
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "*** OnDemandServer shut down error");
                }
                logger.log(Level.INFO, "*** OnDemandServer shut down");
            }
        });
    }

    /**
     * Stop current server
     */
    @Override
    public void stop() throws InterruptedException {
        logger.log(Level.INFO,"OnDemandServer : Server stop, was listening on port " + this.port);
        if (this.server != null) {
            this.server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    @Override
    public void blockUntilShutdown() throws InterruptedException {
        logger.log(Level.INFO,"OnDemandServer : Server blockUntilShutdown, listening on " + this.port);
        if (this.server != null) {
            this.server.awaitTermination();
        }
    }

    private static class OnDemandServerImpl extends GoalStateProvisionerGrpc.GoalStateProvisionerImplBase {

        @Override
        public void requestGoalStates(HostRequest request, StreamObserver<HostRequestReply> responseObserver) {

            logger.log(Level.INFO, "requestGoalStates : receiving request " + request.getStateRequestsCount());

            /////////////////////////////////////////////////////////////////////////////////////////
            //                  On-Demand Algorithm
            // query cache M4 by VNI and source IP, and find all associated list of resource IDs (along with type)
            // Based on the resource type and id, query cache M3 to find its detailed state
            // 1. For resource type == NEIGHBOR, check if there exists resource.IP == request.destination_ip
            //                                  => yes, go to step 2 | no, reject
            // 2. If this port is SG enabled, go to step 3 | no, skip Step 3 and go to Step 4
            // 3. For resource type == SECURITY_GROUP, assuming that this packet must be outbound (as inbound packet has
            //    been handled by SG label), check if the 5-tuples (ip/port, destination ip/port, protocol) + ethertype
            //    comply with the outbound SG rules of source port
            // 3.1 query cache M3 based on associated SG IDs of the source port and retrieve existing SG detail
            // 3.2 for each SG and SG rule, check if outbound rule allows the 5-tuples + ethertype,
            //                             => yes, go to step 4 | no, go to Step 3.3
            // 3.3 (optional) if a rule includes a remote SG id, query cache M3 and retrieve detailed membership of
            //                remote SG, check whether destination id belongs to the remote SG
            //                             => yes, go to Step 4 | no, reject
            // 4. Bingo! this packet is allowed, collect port related resources (NEIGHBOR, SG etc. FULL GS) and send down
            //    to ACA by a separate gRPC client
            /////////////////////////////////////////////////////////////////////////////////////////

            // Step 1: Generate response for each packet based on the above on-demand algorithm
            // if the packet is allowed, set HostRequestReply.HostRequestOperationStatus[request_id].OperationStatus = SUCCESS
            //                           generate GS with port related resources and go to Step 2
            // otherwise, set it to FAILURE and go to Step 3

            //TODO: Implement on-demand algorithm and support setOperationStatus==Failure
            int ind = 0;
            HostRequestReply.Builder replyBuilder = HostRequestReply.newBuilder();
            for (HostRequest.ResourceStateRequest resourceStateRequest: request.getStateRequestsList()) {
                HostRequestReply.HostRequestOperationStatus status =
                        HostRequestReply.HostRequestOperationStatus.newBuilder()
                                .setRequestId(resourceStateRequest.getRequestId())
                                .setOperationStatus(Common.OperationStatus.SUCCESS)
                                .build();
                replyBuilder.setFormatVersion(1)
                        .addOperationStatuses(ind++, status)
                        .buildPartial();
            }
            HostRequestReply reply = replyBuilder.build();
            logger.log(Level.INFO, "requestGoalStates : generate reply " + reply.getOperationStatusesCount());

            // Step 2: Send GS down to target ACA
            //TODO: Populate hostGoalStates based on M2 and M3
            Map<String, HostGoalState> hostGoalStates = new HashMap<>();

            try {
                GoalStateClient grpcGoalStateClient = new GoalStateClientImpl();
                grpcGoalStateClient.sendGoalStates(hostGoalStates);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Step 3: Send response to target ACA
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
