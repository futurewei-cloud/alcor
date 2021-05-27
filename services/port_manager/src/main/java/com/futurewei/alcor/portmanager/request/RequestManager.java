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
        if(request != null){
            requests.add(request);
        }
        if(future != null){
            futures.add(future);
        }
    }

    public void sendRequest(IRestRequest request) throws Exception {
        addFuture(request, null);
        sendRequest(request, null);
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

        LOG.info("Begin to rollback all requests...");

        for (IRestRequest request: requests) {
            request.rollback();
        }
    }
}
