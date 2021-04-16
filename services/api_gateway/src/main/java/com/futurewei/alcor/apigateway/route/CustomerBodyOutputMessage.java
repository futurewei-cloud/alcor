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

package com.futurewei.alcor.apigateway.route;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public class CustomerBodyOutputMessage implements ReactiveHttpOutputMessage {

    private final DataBufferFactory bufferFactory;

    private final HttpHeaders httpHeaders;

    private Flux<DataBuffer> body = Flux.error(new IllegalArgumentException(
            "The body is not set."
    ));

    public CustomerBodyOutputMessage(ServerWebExchange exchange, HttpHeaders httpHeaders) {
        this.bufferFactory = exchange.getResponse().bufferFactory();
        this.httpHeaders = httpHeaders;
    }

    public CustomerBodyOutputMessage(ServerWebExchange exchange) {
        this.bufferFactory = exchange.getResponse().bufferFactory();
        this.httpHeaders = exchange.getResponse().getHeaders();
    }

    @Override
    public DataBufferFactory bufferFactory() {
        return this.bufferFactory;
    }

    @Override
    public void beforeCommit(Supplier<? extends Mono<Void>> action) {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        this.body = Flux.from(body);
        return Mono.empty();
    }

    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return writeWith(Flux.from(body).flatMap(p -> p));
    }

    @Override
    public Mono<Void> setComplete() {
        return writeWith(Flux.empty());
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.httpHeaders;
    }

    /**
     * Return the request body, or an error stream if the body was never set or when.
     * @return body as {@link Flux}
     */
    public Flux<DataBuffer> getBody(){
        return this.body;
    }
}
