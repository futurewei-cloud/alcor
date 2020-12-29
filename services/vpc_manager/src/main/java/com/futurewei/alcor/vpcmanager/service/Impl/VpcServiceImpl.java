package com.futurewei.alcor.vpcmanager.service.Impl;

import com.futurewei.alcor.common.config.Tracing;
import com.futurewei.alcor.common.config.TracingObj;
import com.futurewei.alcor.common.enumClass.NetworkTypeEnum;
import com.futurewei.alcor.vpcmanager.config.JaegerConfig;
import com.futurewei.alcor.vpcmanager.exception.SubnetsNotEmptyException;
import com.futurewei.alcor.vpcmanager.service.SegmentService;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.vpcmanager.service.VpcService;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Scope;
import io.opentracing.Span;
import okhttp3.*;
import com.futurewei.alcor.common.config.JaegerTracerHelper;

import javax.servlet.http.HttpServletRequest;

@Service
public class VpcServiceImpl implements VpcService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SegmentService segmentService;

    @Value("${microservices.route.service.url}")
    private String routeUrl;

    @Autowired private JaegerConfig config;

    @Autowired
    private HttpServletRequest request1;

    private RestTemplate restTemplate = new RestTemplate();

    /**
     * Get route rule info
     * @param vpcId
     * @param vpcState
     * @return route state
     */
    @Override
    @DurationStatistics
    public RouteWebJson getRoute(String vpcId, VpcEntity vpcState, Map<String,String> httpHeaders) throws IOException {
        String serviceName = "VpcService";
        try (JaegerTracer tracer = new JaegerTracerHelper().initTracer(serviceName, config.getJaegerHost(), config.getJaegerPort(), config.getJaegerFlush(), config.getJaegerMaxQsize())) {
            TracingObj tracingObj = Tracing.startSpan(request1,tracer, serviceName);
            Span span=tracingObj.getSpan();
            try (Scope op = tracer.scopeManager().activate(span)) {
                String routeManagerServiceUrl2 = routeUrl + "vpcs/" + vpcId + "/routes";
                ExclusionStrategy myExclusionStrategy =
                        new ExclusionStrategy() {
                            @Override
                            public boolean shouldSkipField(FieldAttributes fa) {
                                String ignoreField = "tenantId";
                                return fa.getName().equals(ignoreField);
                            }

                            @Override
                            public boolean shouldSkipClass(Class<?> clazz) {
                                return false;
                            }
                        };
                Gson gson = new GsonBuilder().setExclusionStrategies(myExclusionStrategy).create();
                String jsonString = gson.toJson(new VpcWebJson(vpcState));
                Response response=Tracing.StartImpl(routeManagerServiceUrl2,jsonString,span,tracer,"POST");
                String rs = response.body().string();
                RouteWebJson rwj = gson.fromJson(rs, RouteWebJson.class);
                return rwj;

            }catch (IOException e)
            {
                logger.error("create route error, {}", e.getMessage());
                throw e;
            }
            finally {
                span.finish();
            }
        }
    }

    /**
     * Allocate a segment for the network
     * @param vpcEntity
     * @return
     * @throws Exception
     */
    @Override
    @DurationStatistics
    public VpcEntity allocateSegmentForNetwork(VpcEntity vpcEntity) throws Exception {
        String networkTypeId = UUID.randomUUID().toString();
        if (vpcEntity == null) {
            return vpcEntity;
        }

        String networkType = vpcEntity.getNetworkType();
        Long key = null;
        if (networkType == null) {
            // create a vxlan type segment as default
            key = this.segmentService.addVxlanEntity( networkTypeId, NetworkTypeEnum.VXLAN.getNetworkType(), vpcEntity.getId(), vpcEntity.getMtu());

            vpcEntity.setNetworkType(NetworkTypeEnum.VXLAN.getNetworkType());

        } else if (networkType.equals(NetworkTypeEnum.VXLAN.getNetworkType())) {
            key = this.segmentService.addVxlanEntity( networkTypeId, NetworkTypeEnum.VXLAN.getNetworkType(), vpcEntity.getId(), vpcEntity.getMtu());
        } else if (networkType.equals(NetworkTypeEnum.VLAN.getNetworkType())) {
            key = this.segmentService.addVlanEntity( networkTypeId, NetworkTypeEnum.VLAN.getNetworkType(), vpcEntity.getId(), vpcEntity.getMtu());
        } else if (networkType.equals(NetworkTypeEnum.GRE.getNetworkType())) {
            key = this.segmentService.addGreEntity( networkTypeId, NetworkTypeEnum.GRE.getNetworkType(), vpcEntity.getId(), vpcEntity.getMtu());
        }

        if (key != null) {
            vpcEntity.setSegmentationId(Integer.parseInt(String.valueOf(key)));
        }

        return vpcEntity;
    }

    /**
     * check subnets in network are empty or not
     * @param vpcEntity
     * @return
     * @throws SubnetsNotEmptyException
     */
    @Override
    @DurationStatistics
    public boolean checkSubnetsAreEmpty(VpcEntity vpcEntity) throws SubnetsNotEmptyException {
        if (vpcEntity == null) {
            return true;
        }
        List<String> subnets = vpcEntity.getSubnets();
        if (!(subnets == null || subnets.size() == 0)) {
            throw new SubnetsNotEmptyException();
        }
        return true;
    }
}
