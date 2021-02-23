package com.futurewei.alcor.nodemanager.request;

import com.futurewei.alcor.common.executor.AsyncExecutor;
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

    private void sendRequest(IRestRequest request) throws Exception {
        request.send();
    }

    /**
     * Guarantee that multiple threads cannot visit futures at the same time
     */
    private void addFuture(IRestRequest request, CompletableFuture future) {
        requests.add(request);
        futures.add(future);
    }

    public void sendRequestAsync(IRestRequest request) {
        CompletableFuture future = CompletableFuture.supplyAsync(() -> {
            try {
                sendRequest(request);
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

}
