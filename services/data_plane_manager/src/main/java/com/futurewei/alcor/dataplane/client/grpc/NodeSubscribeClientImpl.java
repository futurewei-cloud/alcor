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

import com.futurewei.alcor.dataplane.client.NodeSubscribeClient;
import com.futurewei.alcor.dataplane.config.Config;
import com.futurewei.alcor.schema.SubscribeInfoProvisionerGrpc;
import com.futurewei.alcor.schema.Subscribeinfoprovisioner;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

@Component
public class NodeSubscribeClientImpl implements NodeSubscribeClient {
    private static final Logger LOG = LoggerFactory.getLogger(DataPlaneClientImpl.class);

    private int grpcPort;

    private final ExecutorService executor;

    @Autowired
    public NodeSubscribeClientImpl(Config globalConfig) {
        this.grpcPort = globalConfig.topicGrpcPort;
        this.executor = new ThreadPoolExecutor(globalConfig.grpcMinThreads,
                globalConfig.grpcMaxThreads,
                50,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                new DefaultThreadFactory(globalConfig.grpThreadsName));
    }

    @Override
    public Map<String, Subscribeinfoprovisioner.SubscribeOperationReply> asyncSendSubscribeInfos(Map<String, Subscribeinfoprovisioner.NodeSubscribeInfo> subscribeInfoMap) throws Exception {
        Map<String, Subscribeinfoprovisioner.SubscribeOperationReply> results = new HashMap<>();

        CountDownLatch finished = new CountDownLatch(subscribeInfoMap.size());

        for (Map.Entry<String, Subscribeinfoprovisioner.NodeSubscribeInfo> entry : subscribeInfoMap.entrySet()) {
            asyncSendSubscribeInfo(entry.getKey(),
                    entry.getValue(),
                    new StreamObserver<Subscribeinfoprovisioner.SubscribeOperationReply>() {
                        @Override
                        public void onNext(Subscribeinfoprovisioner.SubscribeOperationReply subscribeOperationReply) {
                            results.put(entry.getKey(), subscribeOperationReply);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            finished.countDown();
                        }

                        @Override
                        public void onCompleted() {
                            finished.countDown();
                        }
                    }
            );
        }

        try {
            finished.await(Config.SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return results;
    }

    private void asyncSendSubscribeInfo(String hostIp,
                                        Subscribeinfoprovisioner.NodeSubscribeInfo nodeSubscribeInfo,
                                        StreamObserver<Subscribeinfoprovisioner.SubscribeOperationReply> observer) {
        ManagedChannel channel = newChannel(hostIp, grpcPort);
        SubscribeInfoProvisionerGrpc.SubscribeInfoProvisionerStub asyncStub = SubscribeInfoProvisionerGrpc.newStub(channel).withExecutor(executor);
        asyncStub.pushNodeSubscribeInfo(nodeSubscribeInfo, observer);
    }

    private ManagedChannel newChannel(String host, int port) {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }
}
