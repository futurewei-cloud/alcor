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

package com.futurewei.alcor.dataplane.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.enumClass.StatusEnum;
import com.futurewei.alcor.common.executor.AsyncExecutor;
import com.futurewei.alcor.dataplane.cache.VpcGatewayInfoCache;
import com.futurewei.alcor.dataplane.entity.MulticastGoalState;
import com.futurewei.alcor.dataplane.entity.UnicastGoalState;
import com.futurewei.alcor.dataplane.entity.ZetaPortGoalState;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.gateway.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service("zetaGatewayClient")
public class ZetaGatewayClient {
    private static final Logger LOG = LoggerFactory.getLogger(ZetaGatewayClient.class);

    @Autowired
    private VpcGatewayInfoCache gatewayInfoCache;

    @Value("${zetaGateway.check.timeout}")
    private String zetaGatewayCheckTimeout;

    @Value("${zetaGateway.check.interval}")
    private String zetaGatewayCheckInterval;

    @Value("${microservices.zeta.management.url}")
    private String zetaGatewayUrl;

    @Value("${microservices.gateway.service.url}")
    private String gatewayUrl;

    private final RestTemplate restTemplate;

    public ZetaGatewayClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public void updateVPCZetaGateway(GatewayInfo gatewayInfo) throws Exception {
        String url = gatewayUrl + "project/" + "/gatewayinfo/" + gatewayInfo.getResourceId();
        HttpEntity<GatewayInfoJson> gatewayHttpEntity = new HttpEntity<>(new GatewayInfoJson(gatewayInfo));
        restTemplate.put(url, gatewayHttpEntity, ResponseId.class);
    }

    public void enableZetaGatewayForPort(InternalPortEntity portEntity) throws Exception {
        GatewayInfo gatewayInfo = gatewayInfoCache.findItem(portEntity.getVpcId());
        if (gatewayInfo == null) {
            List<GatewayEntity> newGatewayEntities = new ArrayList<>();
            newGatewayEntities.add(new GatewayEntity(null, null, null, null, GatewayType.ZETA,
                    StatusEnum.NOTAVAILABLE.getStatus(), null, null, null, null, null, null));
            GatewayInfo newGatewayInfo = new GatewayInfo(portEntity.getVpcId(), newGatewayEntities, null, "available");
            gatewayInfoCache.addItem(newGatewayInfo);
            LOG.error("Zeta gateway is not ready for VPC {}", portEntity.getVpcId());

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
                                for (GatewayEntity gw : gwInfo.getGatewayEntities()) {
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
                            LOG.error("Zeta gateway failed for VPC {}:{}", portEntity.getVpcId(), e.getMessage());
                            executor.shutdown();
                        }
                    } else {
                        gateway.setStatus(StatusEnum.FAILED.getStatus());
                        // something wrong for the zeta gateway, raise alarm?
                        LOG.error("Zeta gateway failed for VPC {}", portEntity.getVpcId());
                    }
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> sendGoalStates(Object arg1, Object arg2, Object arg3) throws Exception {
        DataPlaneClient dataPlaneClient = (DataPlaneClient) arg1;
        List<UnicastGoalState> unicastGoalStates = (List<UnicastGoalState>) arg2;
        MulticastGoalState multicastGoalState = (MulticastGoalState) arg3;
        return dataPlaneClient.sendGoalStates(unicastGoalStates, multicastGoalState);
    }

    private List<String> processZetaAca(List<Object> results, List<String> failedZetaPorts) {
        List<String> failedHosts = new ArrayList<>();
        List<ZetaPortEntity> zetaports = new ArrayList<>();

        if (results != null) {
            for (Object result : results) {
                if (result instanceof List<?>) {
                    for (Object obj : (List<?>) result) {
                        if (obj instanceof String) {
                            failedHosts.add((String) obj);
                        }
                    }
                } else if (result instanceof ZetaPortsWebJson) {
                    zetaports.addAll(((ZetaPortsWebJson) result).getZetaPorts());
                }
            }
        }

        List<String> fHosts = new ArrayList<>(failedHosts);
        for (String host : failedHosts) {
            for (ZetaPortEntity zeta : zetaports) {
                // zetaport ok, but aca failed, rollback zeta
                if (zeta.getNodeIp().equals(host)) {
                    // rollback zeta
                    break;
                }
            }
            fHosts.remove(host);
        }
        if (fHosts.size() > 0) {
            failedZetaPorts.addAll(fHosts);
            // rollback aca
        }
        return failedHosts;
    }

    private void rollbackZetaAca(List<Object> results, ZetaPortGoalState zetaPortGoalState) {
        if (results != null && results.size() > 0) {
            for (Object result : results) {
                if (result instanceof List<?>) {
                    // rollback zeta
                } else if (result instanceof ZetaPortsWebJson) {
                    // rollback aca
                }
            }
        }
    }


    public ZetaPortsWebJson zetaSendGoalState(Object arg1) throws Exception {
        ZetaPortGoalState zetaPortGoalState = (ZetaPortGoalState) arg1;
        String url = zetaGatewayUrl + "/ports";
        ZetaPortsWebJson zetaPortsWebJson = new ZetaPortsWebJson(zetaPortGoalState.getPortEntities());
        ObjectMapper Obj = new ObjectMapper();
        String jsonStr = Obj.writeValueAsString(zetaPortsWebJson.getZetaPorts());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> zetaHttpEntity = new HttpEntity<>(jsonStr, headers);
        ZetaPortsWebJson result = new ZetaPortsWebJson();

        try {
            if (zetaPortGoalState.getPortEntities().size() > 0) {
                if (Common.OperationType.CREATE.equals(zetaPortGoalState.getOpType())) {
                    Object[] response = restTemplate.postForObject(url, zetaHttpEntity, Object[].class);
                    if (response != null) {
                        ObjectMapper mapper = new ObjectMapper();
                        List<ZetaPortEntity> res = mapper.convertValue(response, new TypeReference<List<ZetaPortEntity>>() {
                        });
                        result.setZetaPorts(res);
                    }
                } else if (Common.OperationType.UPDATE.equals(zetaPortGoalState.getOpType())) {
                    restTemplate.put(url, zetaHttpEntity, ZetaPortsWebJson.class);
                } else if (Common.OperationType.DELETE.equals(zetaPortGoalState.getOpType())) {
                    restTemplate.delete(url, zetaHttpEntity, ZetaPortsWebJson.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<String> sendGoalStateToZetaAcA(List<UnicastGoalState> unicastGoalStates,
                                               MulticastGoalState multicastGoalState,
                                               DataPlaneClient dataPlaneClient,
                                               ZetaPortGoalState zetaPortGoalState,
                                               List<String> failedZetaPorts) throws Exception {
        List<String> failedHosts;
        AsyncExecutor executor = new AsyncExecutor();
        executor.runAsync(this::sendGoalStates, dataPlaneClient, unicastGoalStates, multicastGoalState);
        executor.runAsync(this::zetaSendGoalState, zetaPortGoalState);

        List<Object> results = null;
        try {
            results = executor.joinAll();
            failedHosts = processZetaAca(results, failedZetaPorts);
        } catch (Exception e) {
            LOG.error("", e);
            executor.waitAll();
            rollbackZetaAca(results, zetaPortGoalState);
            throw e;
        }
        return failedHosts;
    }
}