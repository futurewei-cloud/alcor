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
package com.futurewei.alcor.netwconfigmanager.server.grpc;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.netwconfigmanager.client.GoalStateClient;
import com.futurewei.alcor.netwconfigmanager.client.gRPC.GoalStateClientImpl;
import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.netwconfigmanager.server.NetworkConfigServer;
import com.futurewei.alcor.netwconfigmanager.service.GoalStatePersistenceService;
import com.futurewei.alcor.netwconfigmanager.service.OnDemandService;
import com.futurewei.alcor.netwconfigmanager.util.DemoUtil;
import com.futurewei.alcor.netwconfigmanager.util.NetworkConfigManagerUtil;
import com.futurewei.alcor.schema.*;
import io.grpc.stub.StreamObserver;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Component
@Configurable
@ComponentScan(value = "com.futurewei.alcor.netwconfigmanager.service")
public class GoalStateProvisionerServer implements NetworkConfigServer {
    private static final Logger logger = LoggerFactory.getLogger();

    private final int responseDefaultFormatVersion = 1;
    private final int port;
    private final Server server;

    // each host_ip should have this amount of gRPC channels.
    @Value("${grpc.number-of-channels-per-host:1}")
    private int numberOfGrpcChannelPerHost;

    // when a channel is set up, send this amount of default GoalStates for warmup.
    @Value("${grpc.number-of-warmups-per-channel:1}")
    private int numberOfWarmupsPerChannel;

    @Value("${grpc.monitor-hosts}")
    private ArrayList<String> monitorHosts;

    @Autowired
    private OnDemandService onDemandService;

    @Autowired
    private GoalStatePersistenceService goalStatePersistenceService;

//    @Autowired
//    private GoalStateClient grpcGoalStateClient;

    public GoalStateProvisionerServer() {
        this.port = 9016; // TODO: make this configurable

        IpInterceptor clientIpInterceptor = new IpInterceptor();
        this.server = ServerBuilder.forPort(this.port)
                .addService(new GoalStateProvisionerImpl(clientIpInterceptor))
                .maxInboundMessageSize(Integer.MAX_VALUE)
                .maxInboundMetadataSize(Integer.MAX_VALUE)
                .intercept(clientIpInterceptor)
                .build();
    }

    @PostConstruct
    public void checkServices() {
        if (onDemandService == null) {
            logger.log(Level.SEVERE, "[requestGoalStates] onDemandService is null");
        }
        if (goalStatePersistenceService == null) {
            logger.log(Level.SEVERE, "[requestGoalStates] goalStatePersistenceService is null");
        }
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
        logger.log(Level.INFO, "GoalStateProvisionerServer : Server stop, was listening on " + this.port);
        if (this.server != null) {
            this.server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    @Override
    public void blockUntilShutdown() throws InterruptedException {
        logger.log(Level.INFO, "GoalStateProvisionerServer : Server blockUntilShutdown, listening on " + this.port);
        if (this.server != null) {
            this.server.awaitTermination();
        }
    }

    private class GoalStateProvisionerImpl extends GoalStateProvisionerGrpc.GoalStateProvisionerImplBase {

        private IpInterceptor ipInterceptor;

        GoalStateProvisionerImpl(IpInterceptor ipInterceptor) {
            this.ipInterceptor = ipInterceptor;
        }

        @Override
        @DurationStatistics
        public StreamObserver<Goalstate.GoalStateV2> pushGoalStatesStream(final StreamObserver<Goalstateprovisioner.GoalStateOperationReply> responseObserver) {

            return new StreamObserver<Goalstate.GoalStateV2>() {
                @Override
                public void onNext(Goalstate.GoalStateV2 value) {

                    logger.log(Level.INFO, "pushGoalStatesStream : receiving GS V2 message " + value.toString());
                    long start = System.currentTimeMillis();

                    //prepare GS message based on host
                    Map<String, HostGoalState> hostGoalStates = NetworkConfigManagerUtil.splitClusterToHostGoalState(value);

                    //store the goal state in cache
                    Set<String> processedResourceIds = new HashSet<>();
                    for (Map.Entry<String, HostGoalState> entry : hostGoalStates.entrySet()) {
                        String hostId = entry.getKey();
                        HostGoalState hostGoalState = entry.getValue();

                        try {
                            goalStatePersistenceService.updateGoalState(hostId, hostGoalState);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    long end = System.currentTimeMillis();
                    logger.log(Level.FINE, "pushGoalStatesStream : finished putting GS into cache, elapsed time in milliseconds: " + + (end-start));
                    // filter neighbor/SG update, and send them down to target ACA
                    try {
                        Map<String, HostGoalState> filteredGoalStates = NetworkConfigManagerUtil.filterNeighbors(hostGoalStates);

                        GoalStateClient grpcGoalStateClient =  GoalStateClientImpl.getInstance(numberOfGrpcChannelPerHost, numberOfWarmupsPerChannel, monitorHosts);

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
                    long end1 = System.currentTimeMillis();
                    logger.log(Level.FINE, "pushGoalStatesStream : Replied to DPM, from received to replied, elapsed time in milliseconds: " + + (end1-end));
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
        @DurationStatistics
        public void requestGoalStates(Goalstateprovisioner.HostRequest request,
                                      StreamObserver<Goalstateprovisioner.HostRequestReply> responseObserver) {
            long start = System.currentTimeMillis();
            String state_request_uuid = "";
            if (request.getStateRequestsList().size() == 1){
                state_request_uuid = request.getStateRequests(0).getRequestId();
                logger.log(Level.FINE, "requestGoalStates : received HostRequest with UUID: " + state_request_uuid + " at: " + start);
            }
            logger.log(Level.INFO, "requestGoalStates : receiving request " + request.toString());
            logger.log(Level.INFO, "requestGoalStates : receiving request list " + request.getStateRequestsList());
            logger.log(Level.INFO, "requestGoalStates : receiving request count" + request.getStateRequestsCount());

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

            // Step 0: Prepare to retrieve client IP address from gRPC transport
            String clientIpAddress = this.ipInterceptor.getClientIpAddress();
            logger.log(Level.INFO, "[requestGoalStates] Client IP address = " + clientIpAddress);

            // Step 1: Retrieve GoalState from M2/M3 caches and send it down to target ACA
            HashSet<String> failedRequestIds = new HashSet<>();
            try {
                Map<String, HostGoalState> hostGoalStates = new HashMap<>();
                for (Goalstateprovisioner.HostRequest.ResourceStateRequest resourceStateRequest : request.getStateRequestsList()) {
                    HostGoalState hostGoalState = onDemandService.retrieveGoalState(resourceStateRequest, clientIpAddress);

                    if (hostGoalState == null) {
                        logger.log(Level.WARNING, "[requestGoalStates] No resource found for resource state request " +
                                resourceStateRequest.toString());
                        failedRequestIds.add(resourceStateRequest.getRequestId());
                        continue;
                    }

                    String hostIp = hostGoalState.getHostIp();
                    if (hostGoalStates.containsKey(hostIp)) {
                        //Case 1: Handle potential overwrite when more than one requests come from the same host
                        HostGoalState existingHostGoalState = hostGoalStates.get(hostIp);
                        HostGoalState updatedHostGoalState = NetworkConfigManagerUtil.consolidateHostGoalState(existingHostGoalState, hostGoalState);
                        hostGoalStates.put(hostIp, updatedHostGoalState);
                        logger.log(Level.INFO, "[requestGoalStates] Same Host IP detected: " + hostIp +
                                " | existing GS: " + existingHostGoalState.toString() +
                                " | updated GS: " + updatedHostGoalState.toString());
                    } else {
                        //Case 2: new host
                        hostGoalStates.put(hostIp, hostGoalState);
                        logger.log(Level.INFO, "[requestGoalStates] New Host IP: " + hostIp + " | GS: ",
                                hostGoalState.toString());
                    }
                }

                GoalStateClient grpcGoalStateClient = GoalStateClientImpl.getInstance(numberOfGrpcChannelPerHost, numberOfWarmupsPerChannel, monitorHosts);
                long end = System.currentTimeMillis();
                logger.log(Level.FINE, "requestGoalStates : Pushing GS with UUID: " + state_request_uuid + " at: " + end);
                grpcGoalStateClient.sendGoalStates(hostGoalStates);
                logger.log(Level.FINE, "[requestGoalStates] From retrieving goalstate to sent goalstate to host, elapsed Time in milli seconds: "+ (end-start));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "[requestGoalStates] Retrieve from host fails. IP address = " + clientIpAddress);
                e.printStackTrace();
            }

            // Step 2: Generate response for each packet based on the on-demand algorithm
            // if the packet is allowed, set HostRequestReply.HostRequestOperationStatus[request_id].OperationStatus = SUCCESS
            //                           generate GS with port related resources (completed at Step 1) and go to Step 3
            // otherwise, set it to FAILURE and go to Step 3
            int ind = 0;
            Goalstateprovisioner.HostRequestReply.Builder replyBuilder = Goalstateprovisioner.HostRequestReply.newBuilder();
            for (Goalstateprovisioner.HostRequest.ResourceStateRequest resourceStateRequest : request.getStateRequestsList()) {
                String requestId = resourceStateRequest.getRequestId();
                Goalstateprovisioner.HostRequestReply.HostRequestOperationStatus status;

                if (failedRequestIds.contains(requestId)) {
                    status = Goalstateprovisioner.HostRequestReply.HostRequestOperationStatus.newBuilder()
                            .setRequestId(requestId)
                            .setOperationStatus(Common.OperationStatus.FAILURE)
                            .build();
                } else {
                    status = Goalstateprovisioner.HostRequestReply.HostRequestOperationStatus.newBuilder()
                            .setRequestId(requestId)
                            .setOperationStatus(Common.OperationStatus.SUCCESS)
                            .build();
                }

                replyBuilder.setFormatVersion(responseDefaultFormatVersion)
                        .addOperationStatuses(ind++, status)
                        .buildPartial();
            }
            Goalstateprovisioner.HostRequestReply reply = replyBuilder.build();
            logger.log(Level.INFO, "requestGoalStates : generate reply " + reply.toString());

            // Step 3: Send response to target ACAs
            long end = System.currentTimeMillis();
            logger.log(Level.FINE, "[requestGoalStates] From received hostOperation to before response, elapsed Time in milli seconds: "+ (end-start));
            responseObserver.onNext(reply);
            logger.log(Level.FINE, "requestGoalStates : replying HostRequest with UUID: " + state_request_uuid + " at: " + end);
            responseObserver.onCompleted();
            long end1 = System.currentTimeMillis();
            logger.log(Level.FINE, "requestGoalStates : sent on-demand response to ACA | ",
                    reply.toString() + " took " + (end1-end) + " milliseconds");
        }
    }
}
