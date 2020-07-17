/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.portmanager.request;

import com.futurewei.alcor.common.executor.AsyncExecutor;
import com.futurewei.alcor.portmanager.processor.CallbackFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class RequestManager {
    private static final Logger LOG = LoggerFactory.getLogger(RequestManager.class);

    private List<CompletableFuture> futures = new ArrayList<>();
    private List<IRestRequest> requests = new ArrayList<>();

    private void sendRequest(IRestRequest request, CallbackFunction callback) throws Exception {
        request.send();

        if (callback != null) {
            callback.apply(request);
        }
    }

    /**
     * Guarantee that multiple threads cannot visit futures at the same time
     */
    private void addFuture(IRestRequest request, CompletableFuture future) {
        requests.add(request);
        futures.add(future);
    }

    public void sendRequestAsync(IRestRequest request, CallbackFunction callback) {
        CompletableFuture future = CompletableFuture.supplyAsync(() -> {
            try {
                sendRequest(request, callback);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
            return null;
        }, AsyncExecutor.executor);

        addFuture(request, future);
    }

    /**
     * Guarantee that multiple threads cannot visit futures at the same time, When calling
     * waitAllRequestsFinish we must make sure that all asynchronous methods have been called
     */
    public void waitAllRequestsFinish() {
        Iterator<CompletableFuture> iterator = futures.iterator();
        while (iterator.hasNext()) {
            CompletableFuture future = iterator.next();
            iterator.remove();
            future.join();
        }
    }

    private void waitAllFuturesFinish() {
        Iterator<CompletableFuture> iterator = futures.iterator();
        while (iterator.hasNext()) {
            CompletableFuture future = iterator.next();
            iterator.remove();
            try {
                future.join();
            } catch (Exception e) {
                LOG.error("{} join exception: {}", future, e);
            }
        }
    }

    public void rollbackAllRequests() throws Exception {
        waitAllFuturesFinish();

        LOG.error("Begin to rollback all requests...");

        for (IRestRequest request: requests) {
            request.rollback();
        }
    }
}
