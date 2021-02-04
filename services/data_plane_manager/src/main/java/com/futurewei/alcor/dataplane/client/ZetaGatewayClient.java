package com.futurewei.alcor.dataplane.client;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.enumClass.StatusEnum;
import com.futurewei.alcor.dataplane.cache.VpcGatewayInfoCache;
import com.futurewei.alcor.dataplane.client.grpc.DataPlaneClientImpl;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.gateway.GatewayEntity;
import com.futurewei.alcor.web.entity.gateway.GatewayInfo;
import com.futurewei.alcor.web.entity.gateway.GatewayInfoJson;
import com.futurewei.alcor.web.entity.gateway.GatewayType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@ComponentScan(value = "com.futurewei.alcor.common.stats")
@Service
public class ZetaGatewayClient {
    private static final Logger LOG = LoggerFactory.getLogger(DataPlaneClientImpl.class);

    @Autowired
    private VpcGatewayInfoCache gatewayInfoCache;

    @Value("${zetaGateway.check.timeout}")
    private String zetaGatewayCheckTimeout;

    @Value("${zetaGateway.check.interval}")
    private String zetaGatewayCheckInterval;

    @Value("${microservices.zeta.service.url}")
    private String zetaGatewayUrl;

    @Value("${microservices.gateway.service.url}")
    private String gatewayUrl;

    private RestTemplate restTemplate = new RestTemplate();

    public ZetaGatewayClient() {

    }

//    public ZetaPortsWebJson sendZetaGoalStates(Object arg1) {
//        ZetaPortGoalState zetaPortGoalState = (ZetaPortGoalState) arg1;
//        String url =  zetaGatewayUrl + "/ports";
//        ZetaPortsWebJson zetaPortsWebJson = new ZetaPortsWebJson(zetaPortGoalState.getPortEntities());
//        HttpEntity<ZetaPortsWebJson> zetaHttpEntity = new HttpEntity<>(zetaPortsWebJson);
//        ZetaPortsWebJson result = new ZetaPortsWebJson();
//
//        if (zetaPortGoalState.getPortEntities().size() > 0) {
//            if (Common.OperationType.CREATE.equals(zetaPortGoalState.getOpType())) {
//                result = restTemplate.postForObject(url, zetaHttpEntity, ZetaPortsWebJson.class);
//            } else if (Common.OperationType.UPDATE.equals(zetaPortGoalState.getOpType())) {
//                restTemplate.put(url, zetaHttpEntity, ZetaPortsWebJson.class);
//            } else if (Common.OperationType.DELETE.equals(zetaPortGoalState.getOpType())) {
//                restTemplate.delete(url, zetaHttpEntity, ZetaPortsWebJson.class);
//            }
//        }
//        return result;
//    }

    public void updateVPCZetaGateway(GatewayInfo gatewayInfo) throws Exception {
        String url = gatewayUrl + "project/" + "/gatewayinfo/" + gatewayInfo.getResourceId();
        HttpEntity<GatewayInfoJson> gatewayHttpEntity = new HttpEntity<>(new GatewayInfoJson(gatewayInfo));
        restTemplate.put(url, gatewayHttpEntity, ResponseId.class);
    }

    public void checkZetaGateway(InternalPortEntity portEntity) throws Exception {
        GatewayInfo gatewayInfo = gatewayInfoCache.findItem(portEntity.getVpcId());
        if (gatewayInfo == null) {
            List<GatewayEntity> newGatewayEntities = new ArrayList<>();
            newGatewayEntities.add(new GatewayEntity(null, null, null, null, GatewayType.ZETA,
                    StatusEnum.NOTAVAILABLE.getStatus(), null, null, null, null, null, null));
            GatewayInfo newGatewayInfo = new GatewayInfo(portEntity.getVpcId(), newGatewayEntities, null, "available");
            gatewayInfoCache.addItem(newGatewayInfo);

            // notify GM to update VPC’s zeta gateway status
            updateVPCZetaGateway(newGatewayInfo);
            portEntity.setIsZetaGatewayPort(Boolean.FALSE);
        } else {
            for (GatewayEntity gateway : gatewayInfo.getGatewayEntities()) {
                if (GatewayType.ZETA.equals(gateway.getType())) {
                    if (StatusEnum.READY.getStatus().equals(gateway.getStatus())) {
                        portEntity.setIsZetaGatewayPort(Boolean.TRUE);
                    } else if (StatusEnum.PENDING.getStatus().equals(gateway.getStatus())) {
                        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                        Future<?> future = executor.scheduleAtFixedRate(() -> {
                            try {
                                GatewayInfo gwInfo = gatewayInfoCache.findItem(portEntity.getVpcId());
                                for (GatewayEntity gw: gwInfo.getGatewayEntities()) {
                                    if (GatewayType.ZETA.equals(gw.getType())) {
                                        if (StatusEnum.READY.getStatus().equals(gw.getStatus())) {
                                            portEntity.setIsZetaGatewayPort(Boolean.TRUE);
                                            executor.shutdown();
                                        }
                                        break;
                                    }
                                }
                            } catch (CacheException e) {
                                e.printStackTrace();
                            }
                        }, 0, Long.parseLong(zetaGatewayCheckInterval), TimeUnit.SECONDS);

                        try {
                            future.get(Long.parseLong(zetaGatewayCheckTimeout), TimeUnit.SECONDS);
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            portEntity.setIsZetaGatewayPort(Boolean.FALSE);
                            gateway.setStatus(StatusEnum.FAILED.getStatus());
                            e.printStackTrace();
                            executor.shutdown();
                        }
                    } else {
                        gateway.setStatus(StatusEnum.FAILED.getStatus());
                        // something wrong for the zeta gateway, raise alarm?
                    }
                    break;
                }
            }
        }
    }

}
