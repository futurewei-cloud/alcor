package com.futurewei.alcor.gatewaymanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.executor.AsyncExecutor;
import com.futurewei.alcor.gatewaymanager.config.ExceptionMsgConfig;
import com.futurewei.alcor.gatewaymanager.dao.GWAttachmentRepository;
import com.futurewei.alcor.gatewaymanager.dao.GatewayRepository;
import com.futurewei.alcor.gatewaymanager.entity.*;
import com.futurewei.alcor.web.entity.gateway.GatewayIp;
import com.futurewei.alcor.web.entity.gateway.GatewayIpJson;
import com.futurewei.alcor.web.restclient.GatewayManagerRestClinet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionException;

@Slf4j
@EnableRetry
@Service
public class GatewayService {

    @Autowired
    private GatewayRepository gatewayRepository;

    @Autowired
    private GWAttachmentRepository gwAttachmentRepository;

    @Autowired
    private GatewayManagerRestClinet restClinet;

    public GatewayService() {
    }

    public void createGatewayInfo(VpcInfo vpcInfo) throws CacheException {

        // check whether the project type is zeta

        // create GatewayInfo entity in the DB
        GatewayInfo gatewayInfo = new GatewayInfo();
        GatewayEntity gatewayEntity = new GatewayEntity();
        createGateway(vpcInfo, gatewayInfo, gatewayEntity);

        //store in the GM's DB
        gatewayRepository.addItem(gatewayInfo);

        AsyncExecutor executor = new AsyncExecutor();
        createDPMCacheGateway(executor, gatewayInfo, gatewayEntity);
        try {
            executor.runAsync(args -> restClinet.createVPCInZetaGateway(args), new VpcInfoSub(vpcInfo.getVpcId(), vpcInfo.getVpcVni()));
            updateDPMCacheGateway(executor, gatewayInfo, gatewayEntity);
        } catch (CompletionException e) {
            log.info("failed to create vpc in the Gateway, Exception detail: {}", e.getMessage());
            rollback(executor, gatewayInfo, gatewayEntity);
        }
    }

    //TODO: need to supplemented
    public void updateGatewayInfoForZeta(GatewayInfo newGatewayInfo) throws CacheException {
        GatewayInfo oldGatewayInfo = gatewayRepository.findItem(newGatewayInfo.getResourceId());
        for (GatewayEntity gatewayEntity : newGatewayInfo.getGatewayEntities()) {
            if (gatewayEntity.getType().equals(GatewayType.ZETA)) {
            }
        }

        List<GatewayEntity> gatewayEntities = oldGatewayInfo.getGatewayEntities();
        for (GatewayEntity gatewayEntity : gatewayEntities) {
            for (String attachmentId : gatewayEntity.getAttachments()) {
                GWAttachment gwAttachment = gwAttachmentRepository.findItem(attachmentId);

            }
        }
    }



    private void createGateway(VpcInfo vpcInfo, GatewayInfo gatewayInfo, GatewayEntity gatewayEntity) throws CacheException {
        gatewayEntity.setType(GatewayType.ZETA);
        gatewayEntity.setName(gatewayEntity.getType() + " name");
        gatewayEntity.setState("PENDING");
        gatewayInfo.setResourceId(vpcInfo.getVpcId());
        List<GatewayEntity> gatewayEntities = Collections.singletonList(gatewayEntity);
        gatewayInfo.setGatewayEntities(gatewayEntities);
        gatewayInfo.setStatus("available");
        // create attachment and attach it to the GatewayEntity
        GWAttachment attachment = new GWAttachment(GatewayType.ZETA.getGatewayType() + "attachment - " + ResourceType.VPC.name(),
                ResourceType.VPC, vpcInfo.getVpcId(), gatewayEntity.getId(), "available", vpcInfo.getVpcVni());
        gatewayEntity.setAttachments(Collections.singletonList(attachment.getId()));

        gwAttachmentRepository.addItem(attachment);
    }

    public void deleteGateway(String vpcId, String gatewayId) throws Exception {
        GatewayInfo gatewayInfo = gatewayRepository.findItem(vpcId);
        List<GatewayEntity> gateways = gatewayInfo.getGatewayEntities();
        //Verify that GatewayId exists
        GatewayEntity gateway = gateways.stream().filter(gatewayEntity -> gatewayEntity.getId().equals(gatewayId)).findFirst().orElse(null);
        if (gateway == null) {
            throw new Exception(ExceptionMsgConfig.GATEWAY_ENTITY_NOT_FOUND.getMsg());
        } else {
            gateways.remove(gateway);
        }
        //update the cache of gatewayInfo after remove gatewayId
        gatewayRepository.addItem(gatewayInfo);
    }


    public List<GatewayEntity> getGateways(String resourceId) throws CacheException {
        GatewayInfo gatewayInfo = gatewayRepository.findItem(resourceId);
        return gatewayInfo.getGatewayEntities();
    }

    private void updateDPMCacheGateway(AsyncExecutor executor, GatewayInfo gatewayInfo, GatewayEntity gatewayEntity) {
        List<Object> result = executor.joinAll();
        if (result.size() > 0) {
            for (Object o : result) {
                if (o.getClass().equals(GatewayIpJson.class)) {
                    GatewayIpJson gatewayIpJson = (GatewayIpJson) o;
                    List<GatewayIp> gatewayIps = gatewayIpJson.getGatewayIps();
                    gatewayEntity.setIps(gatewayIps);
                    gatewayEntity.setState("READY");
                }
            }
            restClinet.updateDPMCacheGateway(gatewayInfo);
        }
    }


    @Retryable(maxAttempts = 4)
    private void createDPMCacheGateway(AsyncExecutor executor, GatewayInfo gatewayInfo, GatewayEntity gatewayEntity) {
        executor.runAsync(args -> restClinet.createDPMCacheGateway(args), gatewayInfo);
    }


    @Recover
    private void workAfterRetryFailed(AsyncExecutor executor, GatewayInfo gatewayInfo, GatewayEntity gatewayEntity) {
        rollback(executor, gatewayInfo, gatewayEntity);
    }

    /**
     * rollback if async call failed
     *
     * @param executor
     */
    private void rollback(AsyncExecutor executor, GatewayInfo gatewayInfo, GatewayEntity gatewayEntity) {
        List<Object> result = executor.joinAll();
        log.info("start rollback and update the state for GatewayEntity, the executor's result is: {}", result);
        for (Object ob : result) {
            if (ob.getClass().equals(GatewayIpJson.class)) {
                restClinet.deleteVPCInGateway(ob);
                result.remove(ob);
            }
            if (ob.getClass().equals(String.class)) {
                gatewayEntity.setState("FAILED");
                restClinet.updateDPMCacheGateway(gatewayInfo);
                result.remove(ob);
            }
        }
    }

}
