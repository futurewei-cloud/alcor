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
    @Value("${server.max-header-size:16384}")
    private int maxHeaderSize;

    // In HttpRequestDecoderSpec, the default value of initial line length is 4096 bytes (4KB)
    // but may result in 413 when header is too large.
    // Make the line length configurable
    @Value("${server.initial-line-length: 65536}")
    private int maxInitialLineLength;

    public void customize(NettyReactiveWebServerFactory factory) {
        factory.addServerCustomizers(server ->
                server.httpRequestDecoder(decoder -> decoder.maxHeaderSize(maxHeaderSize)));
        factory.addServerCustomizers(server ->
                server.httpRequestDecoder(decoder -> decoder.maxInitialLineLength(maxInitialLineLength)));
    }
}
