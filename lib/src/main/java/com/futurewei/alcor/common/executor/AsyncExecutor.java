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
package com.futurewei.alcor.common.executor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class AsyncExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncExecutor.class);
    public static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            ThreadPoolExecutorConfig.corePoolSize,
            ThreadPoolExecutorConfig.maximumPoolSize,
            ThreadPoolExecutorConfig.KeepAliveTime,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(ThreadPoolExecutorConfig.capacity),
            new ThreadFactoryBuilder().setNameFormat("selectThreadPoolExecutor-%d").build());
    private List<CompletableFuture> futures;

    public AsyncExecutor() {
        futures = new ArrayList<>();
    }

    public <T>CompletableFuture runAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier);
    }

    public <R>CompletableFuture runAsync(AsyncFunction<Object, R> fun, Object args) throws CompletionException {
        CompletableFuture<R> future = CompletableFuture.supplyAsync(() -> {
            try {
                return fun.apply(args);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor);

        futures.add(future);

        return future;
    }

    public void joinAll() throws CompletionException {
        Iterator<CompletableFuture> iterator = futures.iterator();
        while (iterator.hasNext()) {
            CompletableFuture future = iterator.next();
            iterator.remove();
            future.join();
        }
    }

    public void waitAll() {
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
