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
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.jaegertracing.Configuration;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.grpc.OpenTracingContextKey;
import io.opentracing.contrib.grpc.TracingServerInterceptor;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import io.opentracing.util.GlobalTracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Configurable
@ComponentScan(value = "com.futurewei.alcor.netwconfigmanager.service")
@Component
public class GoalStateProvisionerServer implements NetworkConfigServer {
    private static final Logger logger = LoggerFactory.getLogger();

    private final int responseDefaultFormatVersion = 1;
    private final int port;
    private Server server;
    private Tracer tracer;

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

    @Autowired
    private GoalStateClient grpcGoalStateClient;

    public GoalStateProvisionerServer() {
        System.setProperty("JAEGER_SERVICE_NAME","alcor-ncm");
        this.port = 9016; // TODO: make this configurable

        Tracer t = TracerResolver.resolveTracer();
        this.tracer = t;
        GlobalTracer.registerIfAbsent(this.tracer);
        logger.log(Level.INFO, "Got this resolved tracer: " + t.toString());
        TracingServerInterceptor serverInterceptor = TracingServerInterceptor
                .newBuilder().withTracer(this.tracer)
                .withVerbosity()
                .withStreaming()
                .build();
        logger.log(Level.INFO, "Got this interceptor from tracer: " + serverInterceptor.toString());
        IpInterceptor clientIpInterceptor = new IpInterceptor();
        this.server = NettyServerBuilder.forPort(this.port)
                .addService(serverInterceptor.intercept(new GoalStateProvisionerImpl(clientIpInterceptor)))
                .maxInboundMessageSize(Integer.MAX_VALUE)
                .maxInboundMetadataSize(Integer.MAX_VALUE)
                .intercept(clientIpInterceptor)
                .maxConcurrentCallsPerConnection(10000)
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
                    Span pSpan = tracer.activeSpan();
                    Span span;

                    if(pSpan != null){
                        span = tracer.buildSpan("alcor-ncm-server-pushdown-gs").asChildOf(pSpan.context()).start();
                    }else{
                        span = tracer.buildSpan("alcor-ncm-server-pushdown-gs").start();
                    }
                    logger.log(Level.INFO, "[pushGoalStatesStream] Got parent span: "+(null == pSpan? "null" : pSpan.toString()));
                    logger.log(Level.INFO, "[pushGoalStatesStream] Built child span: "+span.toString());

                    Scope cscope = tracer.scopeManager().activate(span);

                    logger.log(Level.INFO, "pushGoalStatesStream : receiving GS V2 message " + value.toString());
                    long start = System.currentTimeMillis();
                    Span storeGsSpan = tracer.buildSpan("alcor-ncm-server-store-gs").asChildOf(span.context()).start();
                    Scope storageCscope = tracer.scopeManager().activate(storeGsSpan);


                    //store the goal state in cache
                    Map<String, HostGoalState> hostGoalStates = new HashMap<>();
                    try {
                        hostGoalStates = goalStatePersistenceService.updateGoalStates(value);
                    } catch (Exception e) {
                        e.printStackTrace();
                        responseObserver.onError(e);
                    }

                    storeGsSpan.finish();

                    Set<String> processedResourceIds = new HashSet<>();

                    long end = System.currentTimeMillis();
                    logger.log(Level.FINE, "pushGoalStatesStream : finished putting GS into cache, elapsed time in milliseconds: " + + (end-start));
                    Span filterSendGsSpan = tracer.buildSpan("alcor-ncm-server-filter-send-gs").asChildOf(span.context()).start();
                    Scope filterCscope = tracer.scopeManager().activate(filterSendGsSpan);

                    // filter neighbor/SG update, and send them down to target ACA
                    try {
                        Map<String, HostGoalState> filteredGoalStates = NetworkConfigManagerUtil.filterNeighbors(hostGoalStates);
                        GoalStateClient grpcGoalStateClient = GoalStateClientImpl.getInstance(numberOfGrpcChannelPerHost, numberOfWarmupsPerChannel, monitorHosts);

                        //TODO use filteredGoalStates
                        grpcGoalStateClient.sendGoalStates(hostGoalStates);
                    } catch (Exception e) {
                        e.printStackTrace();
                        responseObserver.onError(e);
                    }
                    filterSendGsSpan.finish();
                    Span replyDPMSpan = tracer.buildSpan("alcor-ncm-server-reply-dpm").asChildOf(span.context()).start();
                    Scope replyCscope = tracer.scopeManager().activate(replyDPMSpan);
                    //consolidate response from ACA and send response to DPM
                    Goalstateprovisioner.GoalStateOperationReply reply =
                            Goalstateprovisioner.GoalStateOperationReply.newBuilder()
                                    .setFormatVersion(100)
                                    .build();
                    responseObserver.onNext(reply);
                    long end1 = System.currentTimeMillis();
                    logger.log(Level.FINE, "pushGoalStatesStream : Replied to DPM, from received to replied, elapsed time in milliseconds: " + + (end1-end));
                    replyDPMSpan.finish();
                    span.finish();
                    logger.log(Level.INFO, "[pushGoalStatesStream] Child span after finish: "+span.toString());
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                    logger.log(Level.WARNING, "*** pushGoalStatesStream cancelled");
                    responseObserver.onError(t);
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
            Span pSpan = tracer.activeSpan();

            logger.log(Level.FINE, "[requestGoalStates] Parent span context, tracer_id: " +
                    (null == pSpan? "null" : pSpan.context().toTraceId()) +
                    ", span_id: " +
                    (null == pSpan? "null" : pSpan.context().toSpanId())
            );
            Span span;

            if(pSpan != null){
                span = tracer.buildSpan("alcor-ncm-on-demand").asChildOf(pSpan.context()).start();
            }else{
                span = tracer.buildSpan("alcor-ncm-on-demand").start();
            }
            logger.log(Level.INFO, "[requestGoalStates] Got parent span: "+ (null == pSpan? "null" : pSpan.toString()));
            logger.log(Level.INFO, "[requestGoalStates] Built child span: "+span.toString());

            Scope cscope = tracer.scopeManager().activate(span);
            long start = System.currentTimeMillis();
            long end = 0;
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
                Span retrieveGsSpan = tracer.buildSpan("alcor-ncm-on-demand-retrieve-gs").asChildOf(span.context()).start();
                Scope retrieveCscope = tracer.scopeManager().activate(retrieveGsSpan);
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
                retrieveGsSpan.finish();
                Span sendGsSpan = tracer.buildSpan("alcor-ncm-on-demand-send-gs").asChildOf(span.context()).start();
                Scope sendCscope = tracer.scopeManager().activate(sendGsSpan);

                GoalStateClient grpcGoalStateClient = GoalStateClientImpl.getInstance(numberOfGrpcChannelPerHost, numberOfWarmupsPerChannel, monitorHosts);
                end = System.currentTimeMillis();
                logger.log(Level.FINE, "requestGoalStates : Pushing GS with UUID: " + state_request_uuid + " at: " + end);
                grpcGoalStateClient.sendGoalStates(hostGoalStates);
                sendGsSpan.finish();
                logger.log(Level.FINE, "[requestGoalStates] From retrieving goalstate to before sending goalstate to host, elapsed Time in milli seconds: "+ (end-start));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "[requestGoalStates] Retrieve from host fails. IP address = " + clientIpAddress);
                e.printStackTrace();
            }
            long endx = System.currentTimeMillis();
            // Step 2: Generate response for each packet based on the on-demand algorithm
            // if the packet is allowed, set HostRequestReply.HostRequestOperationStatus[request_id].OperationStatus = SUCCESS
            //                           generate GS with port related resources (completed at Step 1) and go to Step 3
            // otherwise, set it to FAILURE and go to Step 3
            Span replyOnDemandSpan = tracer.buildSpan("alcor-ncm-on-demand-reply").asChildOf(span.context()).start();
            Scope replyCscope = tracer.scopeManager().activate(replyOnDemandSpan);
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
            long endy = System.currentTimeMillis();
            logger.log(Level.FINE, "[requestGoalStates] From received hostOperation to before response, elapsed Time in milli seconds: "+ (endy-start));
            logger.log(Level.FINE, "[requestGoalStates] Pushed GoalState to host, elapsed Time in milli seconds: "+ (endx-end));
            responseObserver.onNext(reply);
            logger.log(Level.FINE, "requestGoalStates : replying HostRequest with UUID: " + state_request_uuid + " at: " + endy);
            responseObserver.onCompleted();
            long end1 = System.currentTimeMillis();
            logger.log(Level.FINE, "requestGoalStates : sent on-demand response to ACA"  + " took " + (end1-endy) + " milliseconds | " +
                    reply.toString());
            replyOnDemandSpan.finish();
            span.finish();
            logger.log(Level.INFO, "[requestGoalStates] Child span after finish: "+span.toString());
        }
    }

}
