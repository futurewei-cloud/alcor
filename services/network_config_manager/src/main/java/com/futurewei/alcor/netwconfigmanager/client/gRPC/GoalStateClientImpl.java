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
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service("grpcGoalStateClient")
public class GoalStateClientImpl implements GoalStateClient {
    private static final Logger logger = LoggerFactory.getLogger();

    private int hostAgentPort;

    private final ExecutorService executor;

    //    @Autowired
    public GoalStateClientImpl() {
//        this.grpcPort = globalConfig.targetHostPort;
//        this.executor = new ThreadPoolExecutor(globalConfig.grpcMinThreads,
//                globalConfig.grpcMaxThreads,
//                50,
//                TimeUnit.SECONDS,
//                new LinkedBlockingDeque<>(),
//                new DefaultThreadFactory(globalConfig.grpThreadsName));
        this.hostAgentPort = 50001;
        this.executor = new ThreadPoolExecutor(100,
                200,
                50,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                new DefaultThreadFactory("grpc-thread-pool"));

        //TODO: Setup a connection pool. one ACA, one client.
    }

    @Override
    @DurationStatistics
    public List<String> sendGoalStates(Map<String, HostGoalState> hostGoalStates) throws Exception {
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
    }

    private void doSendGoalState(HostGoalState hostGoalState) throws InterruptedException {

        String hostIp = hostGoalState.getHostIp();
        ManagedChannel channel = ManagedChannelBuilder.forAddress(hostIp, this.hostAgentPort)
                .usePlaintext()
                .build();
        GoalStateProvisionerGrpc.GoalStateProvisionerStub asyncStub = GoalStateProvisionerGrpc.newStub(channel);

        Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>> result = new HashMap<>();
        StreamObserver<Goalstateprovisioner.GoalStateOperationReply> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(Goalstateprovisioner.GoalStateOperationReply reply) {
                logger.log(Level.INFO, "Receive response from ACA@" + hostIp + " | " + reply.toString() );
                result.put(hostIp, reply.getOperationStatusesList());
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "Receive error from ACA@" + hostIp + " |  " + t.getMessage() );
            }

            @Override
            public void onCompleted() {
                logger.log(Level.INFO, "Complete receiving message from ACA@" + hostIp);
            }
        };

        StreamObserver<Goalstate.GoalStateV2> requestObserver = asyncStub.pushGoalStatesStream(responseObserver);
        try {
                Goalstate.GoalStateV2 goalState = hostGoalState.getGoalState();
                logger.log(Level.INFO, "Sending GS to Host " + hostIp + " as follows | " + goalState.toString());

                requestObserver.onNext(goalState);
        } catch (RuntimeException e) {
            // Cancel RPC
            requestObserver.onError(e);
            throw e;
        }
        // Mark the end of requests
        logger.log(Level.INFO, "Sending GS to Host " + hostIp + " is completed");
        requestObserver.onCompleted();

//        shutdown(channel);
    }

    private void shutdown(ManagedChannel channel) {
        try {
            channel.shutdown().awaitTermination(Config.SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING,"Timed out forcefully shutting down connection: {}", e.getMessage());
        }
    }
}
