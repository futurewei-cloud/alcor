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

package com.futurewei.alcor.netwconfigmanager.server.grpc;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.netwconfigmanager.client.GoalStateClient;
import com.futurewei.alcor.netwconfigmanager.client.gRPC.GoalStateClientImpl;
import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.netwconfigmanager.server.NetworkConfigServer;
import com.futurewei.alcor.netwconfigmanager.util.DemoUtil;
import com.futurewei.alcor.netwconfigmanager.util.NetworkConfigManagerUtil;
import com.futurewei.alcor.schema.*;
import io.grpc.stub.StreamObserver;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Service
public class GoalStateProvisionerServer implements NetworkConfigServer {

    private static final Logger logger = LoggerFactory.getLogger();

    private final int port;
    private final Server server;

//    @Autowired
//    private GoalStateClient grpcGoalStateClient;

    public GoalStateProvisionerServer() {
        this.port = 9016;
        this.server = ServerBuilder.forPort(this.port)
                .addService(new GoalStateProvisionerImpl())
                .build();
    }

    @Override
    public void start() throws IOException {
        this.server.start();
        logger.log(Level.INFO, "GoalStateProvisionerServer : Server started, listening on " + this.port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                logger.log(Level.INFO, "*** shutting down gRPC server since JVM is shutting down");
                try {
                    GoalStateProvisionerServer.this.stop();
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "*** gRPC server shut down error");
                }
                logger.log(Level.INFO, "*** server shut down");
            }
        });
    }

    @Override
    public void stop() throws InterruptedException {
        logger.log(Level.INFO,"GoalStateProvisionerServer : Server stop, was listening on " + this.port);
        if (this.server != null) {
            this.server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    @Override
    public void blockUntilShutdown() throws InterruptedException {
        logger.log(Level.INFO,"GoalStateProvisionerServer : Server blockUntilShutdown, listening on " + this.port);
        if (this.server != null) {
            this.server.awaitTermination();
        }
    }

    private static class GoalStateProvisionerImpl extends GoalStateProvisionerGrpc.GoalStateProvisionerImplBase {

        GoalStateProvisionerImpl() { }

        @Override
        public StreamObserver<Goalstate.GoalStateV2> pushGoalStatesStream(final StreamObserver<Goalstateprovisioner.GoalStateOperationReply> responseObserver) {

            return new StreamObserver<Goalstate.GoalStateV2>() {
                @Override
                public void onNext(Goalstate.GoalStateV2 value) {

                    logger.log(Level.INFO, "pushGoalStatesStream : receiving GS V2 message " + value.toString());

                    //prepare GS message based on host
                    Map<String, HostGoalState> hostGoalStates = NetworkConfigManagerUtil.splitClusterToHostGoalState(value);

                    //store the goal state in cache

                    // filter neighbor/SG update, and send them down to target ACA
                    try {
                        Map<String, HostGoalState> filteredGoalStates = NetworkConfigManagerUtil.filterNeighbors(hostGoalStates);

                        GoalStateClient grpcGoalStateClient = new GoalStateClientImpl();
                        grpcGoalStateClient.sendGoalStates(filteredGoalStates);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //consolidate response from ACA and send response to DPM
                    Goalstateprovisioner.GoalStateOperationReply reply =
                            Goalstateprovisioner.GoalStateOperationReply.newBuilder()
                                    .setFormatVersion(100)
                                    .build();
                    responseObserver.onNext(reply);
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                    logger.log(Level.WARNING, "*** pushGoalStatesStream cancelled");
                    responseObserver.onCompleted();
                }

                @Override
                public void onCompleted() {
                    logger.log(Level.INFO, "pushGoalStatesStream : onCompleted() ");
                    responseObserver.onCompleted();
                }
            };
        }

        @Override
        public void requestGoalStates(Goalstateprovisioner.HostRequest request, StreamObserver<Goalstateprovisioner.HostRequestReply> responseObserver) {

            logger.log(Level.INFO, "requestGoalStates : receiving request " + request.toString());

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
            Goalstateprovisioner.HostRequestReply.Builder replyBuilder = Goalstateprovisioner.HostRequestReply.newBuilder();
            for (Goalstateprovisioner.HostRequest.ResourceStateRequest resourceStateRequest: request.getStateRequestsList()) {
                Goalstateprovisioner.HostRequestReply.HostRequestOperationStatus status =
                        Goalstateprovisioner.HostRequestReply.HostRequestOperationStatus.newBuilder()
                                .setRequestId(resourceStateRequest.getRequestId())
                                .setOperationStatus(Common.OperationStatus.SUCCESS)
                                .build();
                replyBuilder.setFormatVersion(1)
                        .addOperationStatuses(ind++, status)
                        .buildPartial();
            }
            Goalstateprovisioner.HostRequestReply reply = replyBuilder.build();
            logger.log(Level.INFO, "requestGoalStates : generate reply " + reply.toString());

            // Step 2: Send GS down to target ACA
            //TODO: Populate hostGoalStates based on M2 and M3
            Map<String, HostGoalState> hostGoalStates = new HashMap<>();
            DemoUtil.populateHostGoalState(hostGoalStates);
            logger.log(Level.INFO, "requestGoalStates : send GS to ACA " + DemoUtil.aca_node_one_ip + " | ",
                    hostGoalStates.get(DemoUtil.aca_node_one_ip).getGoalState().toString());

            try {
                GoalStateClient grpcGoalStateClient = new GoalStateClientImpl();
                grpcGoalStateClient.sendGoalStates(hostGoalStates);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Step 3: Send response to target ACA
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
            logger.log(Level.INFO, "requestGoalStates : send on-demand response to ACA " + DemoUtil.aca_node_one_ip + " | ",
                    reply.toString());
        }
    }
}
