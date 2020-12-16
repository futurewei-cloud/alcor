package com.futurewei.alcor.common.config;

import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.samplers.ConstSampler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JaegerTracerHelper {
    @Autowired
    private JaegerConfig jaegerConfig;

    public JaegerTracer initTracer(String service) {

        Configuration.SamplerConfiguration samplerConfig = new Configuration.SamplerConfiguration()
                .withType(ConstSampler.TYPE)
                .withParam(1);
        Configuration.SenderConfiguration senderConfig = new Configuration.SenderConfiguration()
                .withAgentHost(jaegerConfig.getJaegerHost())
                .withAgentPort(Integer.parseInt(jaegerConfig.getJaegerPort()));
        Configuration.ReporterConfiguration reporterConfig = new Configuration.ReporterConfiguration()
                .withLogSpans(true)
                .withFlushInterval(Integer.parseInt(jaegerConfig.getJaegerFlush()))
                .withMaxQueueSize(Integer.parseInt(jaegerConfig.getJaegerMaxQsize()))
                .withSender(senderConfig);
        return new Configuration(service).withSampler(samplerConfig).withReporter(reporterConfig).getTracer();
    }

}
