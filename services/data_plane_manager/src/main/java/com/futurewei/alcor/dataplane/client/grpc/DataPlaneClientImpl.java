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
package com.futurewei.alcor.dataplane.client.grpc;

import com.futurewei.alcor.dataplane.client.DataPlaneClient;
import com.futurewei.alcor.dataplane.config.Config;
import com.futurewei.alcor.dataplane.entity.MulticastGoalState;
import com.futurewei.alcor.dataplane.entity.UnicastGoalState;
import com.futurewei.alcor.schema.GoalStateProvisionerGrpc;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus;
import com.futurewei.alcor.schema.Goalstateprovisioner.GoalStateOperationReply;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

//@Component
@Service("grpcDataPlaneClient")
public class DataPlaneClientImpl implements DataPlaneClient {
    private static final Logger LOG = LoggerFactory.getLogger(DataPlaneClientImpl.class);

    private int grpcPort;

    private final ExecutorService executor;

    @Autowired
    public DataPlaneClientImpl(Config globalConfig) {
        this.grpcPort = globalConfig.port;
        this.executor = new ThreadPoolExecutor(globalConfig.grpcMinThreads,
                globalConfig.grpcMaxThreads,
                50,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                new DefaultThreadFactory(globalConfig.grpThreadsName));
    }

    @Override
    public List<String> sendGoalStates(
            List<UnicastGoalState> unicastGoalStates) throws Exception {
        return doSendGoalStates(unicastGoalStates);
    }

    @Override
    public List<String> sendGoalStates(
            List<UnicastGoalState> unicastGoalStates, MulticastGoalState multicastGoalState) throws Exception {
        if (unicastGoalStates == null) {
            unicastGoalStates = new ArrayList<>();
        }

        if (multicastGoalState != null &&
                multicastGoalState.getHostIps() != null &&
                multicastGoalState.getGoalState() != null) {
            for (String hostIp: multicastGoalState.getHostIps()) {
                UnicastGoalState unicastGoalState = new UnicastGoalState();
                unicastGoalState.setHostIp(hostIp);
                unicastGoalState.setGoalState(multicastGoalState.getGoalState());

                unicastGoalStates.add(unicastGoalState);
            }
        }

        if (unicastGoalStates.size() > 0) {
            return sendGoalStates(unicastGoalStates);
        }

        return null;
    }

    private List<String> doSendGoalStates(List<UnicastGoalState> unicastGoalStates) {
        List<Future<String>>
                futures = new ArrayList<>(unicastGoalStates.size());
        for (UnicastGoalState unicastGoalState: unicastGoalStates) {
            Future<String> future =
                    executor.submit(() -> {
                try {
                    LOG.debug(unicastGoalState.getGoalState().toString());
                    sendGoalState(unicastGoalState);
                } catch (InterruptedException e) {
                    LOG.debug("Send message to " + unicastGoalState.getHostIp()+ " get message: " );
                    e.printStackTrace();
                    return e.getMessage();
                }

                        return null;
            });

            futures.add(future);
        }

        //Handle all failed hosts
        return futures.parallelStream().filter(Objects::nonNull).map(future -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            return null;
        }).filter(item -> item != null).collect(Collectors.toList());
    }

    private void sendGoalState(UnicastGoalState unicastGoalState) throws InterruptedException {
        doSendGoalState(unicastGoalState.getGoalState(), unicastGoalState.getHostIp());
    }

    private void doSendGoalState(Goalstate.GoalState goalState, String hostIp) {

        Map<String, List<GoalStateOperationStatus>> result = new HashMap<>();
        ManagedChannel channel = newChannel(hostIp, grpcPort);
        GoalStateProvisionerGrpc.GoalStateProvisionerBlockingStub blockingStub =
                GoalStateProvisionerGrpc.newBlockingStub(channel);

        GoalStateOperationReply reply =
                blockingStub.pushNetworkResourceStates(goalState);
        List<GoalStateOperationStatus> statuses =
                reply.getOperationStatusesList();

        result.put(hostIp, statuses);

        shutdown(channel);
    }

    private List<Map<String, List<GoalStateOperationStatus>>> asyncSendGoalStates(
            List<UnicastGoalState> unicastGoalStates) {
        List<Map<String, List<GoalStateOperationStatus>>> result =
                new ArrayList<>();

        CountDownLatch finished = new CountDownLatch(unicastGoalStates.size());

        for (UnicastGoalState unicastGoalState: unicastGoalStates) {
            asyncSendGoalState(unicastGoalState, new StreamObserver<GoalStateOperationReply>() {
                @Override
                public void onNext(GoalStateOperationReply value) {
                    result.add(Collections.singletonMap(unicastGoalState.getHostIp(),
                            value.getOperationStatusesList()));
                }

                @Override
                public void onError(Throwable t) {
                    finished.countDown();
                }

                @Override
                public void onCompleted() {
                    finished.countDown();
                }
            });
        }

        try {
            finished.await(Config.SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.warn("goalState can not finished");
        }
        return result;
    }

    private void asyncSendGoalState(UnicastGoalState unicastGoalState,
                       StreamObserver<GoalStateOperationReply> observer) {
        asyncSendGoalState(unicastGoalState.getGoalState(), unicastGoalState.getHostIp(), observer);
    }

    private void asyncSendGoalState(Goalstate.GoalState goalState, String hostIp,
                       StreamObserver<GoalStateOperationReply> observer) {
        ManagedChannel channel = newChannel(hostIp, grpcPort);
        GoalStateProvisionerGrpc.GoalStateProvisionerStub asyncStub =
                GoalStateProvisionerGrpc.newStub(channel).withExecutor(executor);
        asyncStub.pushNetworkResourceStates(goalState, observer);
    }

    private ManagedChannel newChannel(String host, int port) {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

    private void shutdown(ManagedChannel channel) {
        try {
            channel.shutdown().awaitTermination(Config.SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.warn("Timed out forcefully shutting down connection: {}", e.getMessage());
        }
    }
}
