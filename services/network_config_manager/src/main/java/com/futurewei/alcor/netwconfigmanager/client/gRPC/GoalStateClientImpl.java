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
package com.futurewei.alcor.netwconfigmanager.client.gRPC;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.netwconfigmanager.client.GoalStateClient;
import com.futurewei.alcor.netwconfigmanager.config.Config;
import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.schema.GoalStateProvisionerGrpc;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Goalstateprovisioner;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollSocketChannel;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service("grpcGoalStateClient")
public class GoalStateClientImpl implements GoalStateClient {
    private final int GRPC_CHANNEL_WARMUP_TIME_IN_SECONDS = 5;

    private static GoalStateClientImpl instance = null;

    private static final Logger logger = LoggerFactory.getLogger();

    private int hostAgentPort;

    private final ExecutorService executor;

    // each host_ip should have this amount of gRPC channels
    private int numberOfGrpcChannelPerHost;

    // when a channel is set up, send this amount of default GoalStates for warmup.
    private int numberOfWarmupsPerChannel;

    // prints out UUID and time, when sending a GoalState to any of the monitorHosts
    private ArrayList<String> monitorHosts;

    private ConcurrentHashMap<String, ArrayList<GrpcChannelStub>> hostIpGrpcChannelStubMap;


    public static GoalStateClientImpl getInstance(int numberOfGrpcChannelPerHost, int numberOfWarmupsPerChannel, ArrayList<String> monitorHosts) {
        if (instance == null) {
            instance = new GoalStateClientImpl(numberOfGrpcChannelPerHost, numberOfWarmupsPerChannel, monitorHosts);
        }
        return instance;
    }

    public GoalStateClientImpl(int numberOfGrpcChannelPerHost, int numberOfWarmupsPerChannel, ArrayList<String> monitorHosts) {
        // each host should have at least 1 gRPC channel
        if (numberOfGrpcChannelPerHost < 1) {
            numberOfGrpcChannelPerHost = 1;
        }

        // allow users to not send warmups, if they wish to.
        if (numberOfWarmupsPerChannel < 0) {
            numberOfWarmupsPerChannel = 0;
        }

        this.monitorHosts = monitorHosts;
        logger.log(Level.FINE, "Printing out all monitorHosts");
        for (String host : this.monitorHosts) {
            logger.log(Level.FINE, "Monitoring this host: " + host);
        }
        logger.log(Level.FINE, "Done printing out all monitorHosts");
        this.numberOfGrpcChannelPerHost = numberOfGrpcChannelPerHost;
        this.numberOfWarmupsPerChannel = numberOfWarmupsPerChannel;
        this.hostAgentPort = 50001;

        this.executor = new ThreadPoolExecutor(100,
                200,
                50,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                new DefaultThreadFactory("grpc-thread-pool"));
        //TODO: Setup a connection pool. one ACA, one client.
        this.hostIpGrpcChannelStubMap = new ConcurrentHashMap();
        logger.log(Level.FINE, "This instance has " + numberOfGrpcChannelPerHost + " channels, and " + numberOfWarmupsPerChannel + " warmups");
    }

    @Override
    @DurationStatistics
    public List<String> sendGoalStates(Map<String, HostGoalState> hostGoalStates) throws Exception {
        for (HostGoalState hostGoalState : hostGoalStates.values()){
            this.executor.execute(() -> {
                try {
                    doSendGoalState(hostGoalState);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        // Currently, This return value is not used by any of its callers, so it doesn't matter(?)
        return new ArrayList<String>();
        /*
        List<Future<HostGoalState>>
                futures = new ArrayList<>(hostGoalStates.size());

        for (HostGoalState hostGoalState : hostGoalStates.values()) {
            Future<HostGoalState> future =
                    executor.submit(() -> {
                        try {
                            doSendGoalState(hostGoalState);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return hostGoalState;
                        }

                        return new HostGoalState();
                    });

            futures.add(future);
        }

        //Handle all failed hosts
        return futures.parallelStream().filter(Objects::nonNull).map(future -> {
            try {
                return future.get().getHostIp();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            return null;
        }).collect(Collectors.toList());
        */
    }

    private GrpcChannelStub getOrCreateGrpcChannel(String hostIp) {
        if (!this.hostIpGrpcChannelStubMap.containsKey(hostIp)) {
            this.hostIpGrpcChannelStubMap.put(hostIp, createGrpcChannelStubArrayList(hostIp));
            logger.log(Level.INFO, "[getOrCreateGrpcChannel] Created a channel and stub to host IP: " + hostIp);
        }
        int usingChannelWithThisIndex = ThreadLocalRandom.current().nextInt(0, numberOfGrpcChannelPerHost);
        ManagedChannel chan = this.hostIpGrpcChannelStubMap.get(hostIp).get(usingChannelWithThisIndex).channel;
        //checks the channel status, reconnects if the channel is IDLE

        ConnectivityState channelState = chan.getState(true);
        if (channelState != ConnectivityState.READY && channelState != ConnectivityState.CONNECTING && channelState != ConnectivityState.IDLE) {
            GrpcChannelStub newChannelStub = createGrpcChannelStub(hostIp);
            this.hostIpGrpcChannelStubMap.get(hostIp).set(usingChannelWithThisIndex, newChannelStub);
            logger.log(Level.INFO, "[getOrCreateGrpcChannel] Replaced a channel and stub to host IP: " + hostIp);
        }
        logger.log(Level.FINE, "[getOrCreateGrpcChannel] Using channel and stub index " + usingChannelWithThisIndex + " to host IP: " + hostIp);
        return this.hostIpGrpcChannelStubMap.get(hostIp).get(usingChannelWithThisIndex);
    }

    private ArrayList<GrpcChannelStub> createGrpcChannelStubArrayList(String hostIp) {
        long start = System.currentTimeMillis();
        ArrayList<GrpcChannelStub> arr = new ArrayList<>();
        for (int i = 0; i < numberOfGrpcChannelPerHost; i++) {
            GrpcChannelStub channelStub = createGrpcChannelStub(hostIp);
            warmUpChannelStub(channelStub, hostIp);
            arr.add(channelStub);
        }
        long end = System.currentTimeMillis();
        logger.log(Level.FINE, "[createGrpcChannelStubArrayList] Created " + numberOfGrpcChannelPerHost + " gRPC channel stubs for host " + hostIp + ", elapsed Time in milli seconds: " + (end - start));
        return arr;
    }

    // try to warmup a gRPC channel and its stub, by sending an empty GoalState`.
    void warmUpChannelStub(GrpcChannelStub channelStub, String hostIp) {
        GoalStateProvisionerGrpc.GoalStateProvisionerStub asyncStub = channelStub.stub;

        StreamObserver<Goalstateprovisioner.GoalStateOperationReply> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(Goalstateprovisioner.GoalStateOperationReply reply) {
                logger.log(Level.INFO, "Receive warmup response from ACA@" + hostIp + " | " + reply.toString());
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "Receive warmup error from ACA@" + hostIp + " |  " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.log(Level.INFO, "Complete receiving warmup message from ACA@" + hostIp);
            }
        };

        StreamObserver<Goalstate.GoalStateV2> requestObserver = asyncStub.pushGoalStatesStream(responseObserver);
        long warmup_start = System.currentTimeMillis();
        long current_time = System.currentTimeMillis();
        try {
            /*
            while((current_time - warmup_start <= GRPC_CHANNEL_WARMUP_TIME_IN_SECONDS * 1000)){
                current_time = System.currentTimeMillis();
                requestObserver.onNext(Goalstate.GoalStateV2.getDefaultInstance());
            }
            */
            Goalstate.GoalStateV2 goalState = Goalstate.GoalStateV2.getDefaultInstance();
            logger.log(Level.INFO, "Sending warmup GS to Host " + hostIp + " as follows | " + goalState.toString());
            for (int i = 0; i < numberOfWarmupsPerChannel; i++) {
                requestObserver.onNext(goalState);
            }
        } catch (RuntimeException e) {
            // Cancel RPC
            logger.log(Level.WARNING, "[doSendGoalState] Sending GS, but error happened | " + e.getMessage());
            requestObserver.onError(e);
            throw e;
        }
        // Mark the end of requests
        logger.log(Level.INFO, "Sending warmup GS to Host " + hostIp + " is completed");
        return;
    }

    private GrpcChannelStub createGrpcChannelStub(String hostIp) {
        ManagedChannel a = NettyChannelBuilder.forAddress(hostIp, this.hostAgentPort).usePlaintext().executor(new ThreadPoolExecutor(100,
                200,
                50,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                new DefaultThreadFactory(hostIp))).keepAliveWithoutCalls(true).eventLoopGroup(new EpollEventLoopGroup(4)).channelType(EpollSocketChannel.class).keepAliveTime(Long.MAX_VALUE, TimeUnit.SECONDS).flowControlWindow(1024 * 1024 * 1024).build();
        GoalStateProvisionerGrpc.GoalStateProvisionerStub b = GoalStateProvisionerGrpc.newStub(a);
        return new GrpcChannelStub(a, b);
        /*
        ManagedChannel channel = ManagedChannelBuilder.forAddress(hostIp, this.hostAgentPort)
                .usePlaintext()
                .keepAliveWithoutCalls(true)
                .keepAliveTime(Long.MAX_VALUE, TimeUnit.SECONDS)
                .build();
        GoalStateProvisionerGrpc.GoalStateProvisionerStub asyncStub = GoalStateProvisionerGrpc.newStub(channel);

        return new GrpcChannelStub(channel, asyncStub);
        */
    }

    private void doSendGoalState(HostGoalState hostGoalState) throws InterruptedException {

        String hostIp = hostGoalState.getHostIp();
        logger.log(Level.FINE, "Setting up a channel to ACA on: " + hostIp);
        long start = System.currentTimeMillis();
        long end = 0;
        GrpcChannelStub channelStub = getOrCreateGrpcChannel(hostIp);
        long chan_established = System.currentTimeMillis();
        logger.log(Level.FINE, "[doSendGoalState] Established channel, elapsed Time in milli seconds: " + (chan_established - start));
        GoalStateProvisionerGrpc.GoalStateProvisionerStub asyncStub = channelStub.stub;

        long stub_established = System.currentTimeMillis();
        logger.log(Level.FINE, "[doSendGoalState] Established stub, elapsed Time after channel established in milli seconds: " + (stub_established - chan_established));

        Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>> result = new HashMap<>();

        StreamObserver<Goalstateprovisioner.GoalStateOperationReply> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(Goalstateprovisioner.GoalStateOperationReply reply) {
                logger.log(Level.INFO, "Receive response from ACA@" + hostIp + " | " + reply.toString());
                result.put(hostIp, reply.getOperationStatusesList());
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "Receive error from ACA@" + hostIp + " |  " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.log(Level.INFO, "Complete receiving message from ACA@" + hostIp);
            }
        };

        StreamObserver<Goalstate.GoalStateV2> requestObserver = asyncStub.pushGoalStatesStream(responseObserver);
        long requestObserverEstablished = System.currentTimeMillis();
        logger.log(Level.FINE, "[doSendGoalState] Established RequestObserver, elapsed Time after stub established in milli seconds: " + (requestObserverEstablished - stub_established));
        try {
            long before_get_goalState = System.currentTimeMillis();
            Goalstate.GoalStateV2 goalState = hostGoalState.getGoalState();
            long after_get_goalState = System.currentTimeMillis();
            logger.log(Level.INFO, "Sending GS with size " + goalState.getSerializedSize() + " to Host " + hostIp + " as follows | " + goalState.toString());
            requestObserver.onNext(goalState);
            long after_onNext = System.currentTimeMillis();
            logger.log(Level.FINE, "[doSendGoalState] Get goalstatev2 from HostGoalState in milliseconds: " + (after_get_goalState - before_get_goalState));
            logger.log(Level.FINE, "[doSendGoalState] Call onNext in milliseconds: " + (after_onNext - after_get_goalState));

            if (hostGoalState.getGoalState().getNeighborStatesCount() == 1 && monitorHosts.contains(hostIp)) {
                long sent_gs_time = System.currentTimeMillis();
                // If there's only one neighbor state and it is trying to send it to aca_node_one, the IP of which is now
                // hardcoded) this send goalstate action is probably caused by on-demand workflow, need to record when it
                // sends this goalState so what we can look into this and the ACA log to see how much time was spent.
                String neighbor_id = hostGoalState.getGoalState().getNeighborStatesMap().keySet().iterator().next();
                logger.log(Level.INFO, "Sending neighbor ID: " + neighbor_id + " at: " + sent_gs_time);
            }
        } catch (RuntimeException e) {
            // Cancel RPC
            logger.log(Level.WARNING, "[doSendGoalState] Sending GS, but error happened | " + e.getMessage());
            requestObserver.onError(e);
            throw e;
        }
        // Mark the end of requests
        logger.log(Level.INFO, "Sending GS to Host " + hostIp + " is completed");

        // comment out onCompleted so that the same channel/stub and keep sending next time.
        //        requestObserver.onCompleted();
        end = System.currentTimeMillis();
        long onNext_called = System.currentTimeMillis();
        logger.log(Level.FINE, "[doSendGoalState] Whole function call took time in milliseconds: "+(end - start) +
                " \nFrom established stub to onNext called, elapsed Time after channel established in milli seconds: " + (onNext_called - requestObserverEstablished));
//        shutdown(channel);
    }

    private void shutdown(ManagedChannel channel) {
        try {
            channel.shutdown().awaitTermination(Config.SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Timed out forcefully shutting down connection: {}", e.getMessage());
        }
    }

    private class GrpcChannelStub {
        public ManagedChannel channel;
        public GoalStateProvisionerGrpc.GoalStateProvisionerStub stub;

        public GrpcChannelStub(ManagedChannel channel, GoalStateProvisionerGrpc.GoalStateProvisionerStub stub) {
            this.channel = channel;
            this.stub = stub;
        }
    }
}