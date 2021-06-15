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

    private List<CompletableFuture> futures = new ArrayList<>();

    public <T>CompletableFuture runAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier,executor);
    }

    /**
     * Execute the function(first parameter) asynchronously which has one input parameter
     * and one return value. The type of input parameter is Object, and the return value is R.
     * @param fun The function to be executed
     * @param args The parameter of the function being executed
     * @param <R> The type of return value of the function being executed
     * @return CompletableFuture
     * @throws CompletionException
     */
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

    /**
     * Execute the function asynchronously which has two input parameters and one return value.
     * The two input parameters of the function are Object, and the return value is R.
     * @param fun The function to be executed
     * @param arg1 The parameter of the function being executed
     * @param arg2 The parameter of the function being executed
     * @param <R> The type of return value of the function being executed
     * @return CompletableFuture
     * @throws CompletionException
     */
    public <R>CompletableFuture runAsync(AsyncFunctionWithTwoArgs<Object,Object, R> fun, Object arg1, Object arg2) throws CompletionException {
        CompletableFuture<R> future = CompletableFuture.supplyAsync(() -> {
            try {
                return fun.apply(arg1, arg2);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor);

        futures.add(future);

        return future;
    }

    public <R>CompletableFuture runAsync(AsyncFunctionWithThreeArgs<Object,Object,Object, R> fun, Object arg1, Object arg2, Object arg3) throws CompletionException {
        CompletableFuture<R> future = CompletableFuture.supplyAsync(() -> {
            try {
                return fun.apply(arg1, arg2, arg3);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor);

        futures.add(future);

        return future;
    }

    /**
     * Execute fun1 asynchronously, after the execution of fun1 is finished, the output of fun1
     * is used as the input parameter of fun2 to continue to execute fun2. arg1 is the input
     * parameter of fun1, the output of fun1 is used as the input parameter of fun2. And the output
     * of fun2 as the final return value.
     * @param fun1 The function to be executed
     * @param fun2 The function to be executed after the execution of fun1 is finished
     * @param arg1 Input parameters of fun1
     * @param <R> Return Type of fun1
     * @param <U> Return Type of fun2
     * @return CompletableFuture
     * @throws CompletionException
     */
    public <R, U>CompletableFuture runAsyncThenApply(AsyncFunction<Object, R> fun1, AsyncFunction<Object, U> fun2, Object arg1) throws CompletionException {
        CompletableFuture<U> future = CompletableFuture.supplyAsync(() -> {
            try {
                return fun1.apply(arg1);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor).thenApply((ret) -> {
            try {
                return fun2.apply(ret);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });

        futures.add(future);

        return future;
    }

    /**
     * Execute fun1 asynchronously, After the execution of fun1 is finished, the output of fun1
     * is used as the input parameter of fun2 to continue to execute fun2. arg1 is the input
     * parameter of fun1, and arg2 is the input parameter of fun2.And the output of fun2 as the
     * final return value.
     * @param fun1 The function to be executed
     * @param fun2 The function to be executed after the execution of fun1 is finished
     * @param arg1 Input parameters of fun1
     * @param arg2 Input parameters of fun2
     * @param <R> Return Type of fun1
     * @param <U> Return Type of fun2
     * @return CompletableFuture
     * @throws CompletionException
     */
    public <R, U>CompletableFuture runAsyncThenApply(AsyncFunction<Object, R> fun1, AsyncFunction<Object, U> fun2, Object arg1, Object arg2) throws CompletionException {
        CompletableFuture<U> future = CompletableFuture.supplyAsync(() -> {
            try {
                return fun1.apply(arg1);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor).thenApply((ret) -> {
            try {
                return fun2.apply(arg2);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });

        futures.add(future);

        return future;
    }

    /**
     * Execute fun1 asynchronously, after the execution of fun1 is finished, the output of fun1
     * is used as the input parameter of fun2 to continue to execute fun2. arg1 is the input
     * parameter of fun1, the output of fun1 is used as the input parameter of fun2. And the output
     * of fun1 as the final return value.
     * @param fun1 The function to be executed
     * @param fun2 The function to be executed after the execution of fun1 is finished
     * @param arg1 Input parameters of fun1
     * @param <R> Return Type of fun1
     * @param <U> Return Type of fun2
     * @return CompletableFuture
     * @throws CompletionException
     */
    public <R, U>CompletableFuture runAsyncThenAccept(AsyncFunction<Object, R> fun1, AsyncFunction<Object, U> fun2, Object arg1) throws CompletionException {
        CompletableFuture<R> future = CompletableFuture.supplyAsync(() -> {
            try {
                return fun1.apply(arg1);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor).thenApply((ret) -> {
            try {
                fun2.apply(ret);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
            return ret;
        });

        futures.add(future);

        return future;
    }

    /**
     * Execute fun1 asynchronously, After the execution of fun1 is finished, the output of fun1
     * is used as the input parameter of fun2 to continue to execute fun2. arg1 is the input
     * parameter of fun1, and arg2 is the input parameter of fun2. And the output of fun1 as the
     * final return value.
     * @param fun1 The function to be executed
     * @param fun2 The function to be executed after the execution of fun1 is finished
     * @param arg1 Input parameters of fun1
     * @param arg2 Input parameters of fun2
     * @param <R> Return Type of fun1
     * @param <U> Return Type of fun2
     * @return CompletableFuture
     * @throws CompletionException
     */
    public <R, U>CompletableFuture runAsyncThenAccept(AsyncFunction<Object, R> fun1, AsyncFunction<Object, U> fun2, Object arg1, Object arg2) throws CompletionException {
        CompletableFuture<R> future = CompletableFuture.supplyAsync(() -> {
            try {
                return fun1.apply(arg1);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor).thenApply((ret) -> {
            try {
                fun2.apply(arg2);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
            return ret;
        });

        futures.add(future);

        return future;
    }

    /**
     * Execute fun1 asynchronously, After the execution of fun1 is finished, the output of fun1
     * is used as the input parameter of fun2 to continue to execute fun2. arg1 is the input
     * parameter of fun1, the output of fun1 and arg2 is the input parameter of fun2. And the
     * output of fun1 as the final return value.
     * @param fun1 The function to be executed
     * @param fun2 The function to be executed after the execution of fun1 is finished
     * @param arg1 Input parameters of fun1
     * @param arg2 Input parameters of fun2
     * @param <R> Return Type of fun1
     * @param <U> Return Type of fun2
     * @return CompletableFuture
     * @throws CompletionException
     */
    public <R, U>CompletableFuture runAsyncThenAccept(AsyncFunction<Object, R> fun1, AsyncFunctionWithTwoArgs<Object, Object, U> fun2, Object arg1, Object arg2) throws CompletionException {
        CompletableFuture<R> future = CompletableFuture.supplyAsync(() -> {
            try {
                return fun1.apply(arg1);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor).thenApply((ret) -> {
            try {
                fun2.apply(ret, arg2);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
            return ret;
        });

        futures.add(future);

        return future;
    }

    /**
     * Wait for all asynchronous function execution to complete. If an exception occurs
     * in a function, stop waiting and throw a CompletionException exception.
     * @return The results returned by all asynchronous functions
     * @throws CompletionException
     */
    public List<Object> joinAll() throws CompletionException {
        Iterator<CompletableFuture> iterator = futures.iterator();
        List<Object> results = new ArrayList<>();

        while (iterator.hasNext()) {
            CompletableFuture future = iterator.next();
            iterator.remove();
            results.add(future.join());
        }

        return results;
    }

    /**
     * Wait for all asynchronous function execution to complete. if any function has an exception,
     * print the exception log and continue to wait for all functions to finish execution.
     */
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

    public List<Object> joinAllAsync() {
        Iterator<CompletableFuture> iterator = futures.iterator();
        List<Object> results = new ArrayList<>();
        while (iterator.hasNext()) {
            CompletableFuture future = iterator.next();
            iterator.remove();

            try {
                results.add(future.join());
            } catch (Exception e) {
                LOG.error("{} join exception: {}", future, e);
            }
        }
        return results;
    }
}
