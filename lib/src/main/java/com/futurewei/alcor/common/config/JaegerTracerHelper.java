package com.futurewei.alcor.common.config;

import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.samplers.ConstSampler;
import org.springframework.stereotype.Component;

@Component
public class JaegerTracerHelper {

    public JaegerTracer initTracer(String service,String host,int port,int flush,int maxQsize) {

        Configuration.SamplerConfiguration samplerConfig = new Configuration.SamplerConfiguration()
                .withType(ConstSampler.TYPE)
                .withParam(1);
        Configuration.SenderConfiguration senderConfig = new Configuration.SenderConfiguration()
                .withAgentHost(host)
                .withAgentPort(port);
        Configuration.ReporterConfiguration reporterConfig = new Configuration.ReporterConfiguration()
                .withLogSpans(true)
                .withFlushInterval(flush)
                .withMaxQueueSize(maxQsize)
                .withSender(senderConfig);
        return new Configuration(service).withSampler(samplerConfig).withReporter(reporterConfig).getTracer();
    }

}
