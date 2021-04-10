/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/



package com.futurewei.alcor.apigateway.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class WebServerConfiguration implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

    // In HttpRequestDecoderSpec, the default value of max header size is 8,192 bytes (8KB)
    // but may result in 413 when header is too large.
    // Make the header size configurable
    @Value("${server.max-http-header-size:16384}")
    private int maxHttpHeaderSize;

    // In HttpRequestDecoderSpec, the default value of initial line length is 4096 bytes (4KB)
    // but may result in 413 when header is too large.
    // Make the line length configurable
    @Value("${server.initial-line-length: 65536}")
    private int maxInitialLineLength;

    public void customize(NettyReactiveWebServerFactory factory) {
        factory.addServerCustomizers(server ->
                server.httpRequestDecoder(decoder -> decoder.maxHeaderSize(maxHttpHeaderSize)));
        factory.addServerCustomizers(server ->
                server.httpRequestDecoder(decoder -> decoder.maxInitialLineLength(maxInitialLineLength)));
    }
}
