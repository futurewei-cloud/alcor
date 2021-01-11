package com.futurewei.alcor.gatewaymanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.executor.AsyncExecutor;
import com.futurewei.alcor.gatewaymanager.config.ExceptionMsgConfig;
import com.futurewei.alcor.gatewaymanager.dao.GWAttachmentRepository;
import com.futurewei.alcor.gatewaymanager.dao.GatewayRepository;
import com.futurewei.alcor.gatewaymanager.entity.GWAttachment;
import com.futurewei.alcor.gatewaymanager.entity.ResourceType;
import com.futurewei.alcor.web.entity.gateway.VpcInfo;
import com.futurewei.alcor.web.entity.gateway.VpcInfoSub;
import com.futurewei.alcor.web.entity.gateway.*;
import com.futurewei.alcor.web.restclient.GatewayManagerRestClinet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    public void createGatewayInfo(String projectId, VpcInfo vpcInfo) throws CacheException {

        // check whether the project type is zeta

        // create GatewayInfo entity in the DB
        GatewayInfo gatewayInfo = new GatewayInfo();
        GatewayEntity gatewayEntity = new GatewayEntity();
        createGatewayAndAttachment(vpcInfo, gatewayInfo, gatewayEntity);

        //store in the GM's DB
        gatewayRepository.addItem(gatewayInfo);

        AsyncExecutor executor = new AsyncExecutor();
        createDPMCacheGateway(executor, gatewayInfo, gatewayEntity, projectId);
        try {
            executor.runAsync(restClinet::createVPCInZetaGateway, new VpcInfoSub(vpcInfo.getVpcId(), vpcInfo.getVpcVni()));
        } catch (CompletionException e) {
            log.info("failed to create vpc in the Gateway, Exception detail: {}", e.getMessage());
            rollback(executor, gatewayInfo, gatewayEntity, projectId);
        }
        updateDPMCacheGateway(executor, gatewayInfo, gatewayEntity, projectId);
    }

    //TODO: need to supplemented
    public void updateGatewayInfoForZeta(String projectId, GatewayInfo newGatewayInfo) throws CacheException {
        GatewayInfo oldGatewayInfo = gatewayRepository.findItem(newGatewayInfo.getResourceId());
        List<GatewayEntity> gatewayEntities = oldGatewayInfo.getGatewayEntities();

        Map<String, GWAttachment> attachmentsMap = gwAttachmentRepository.findAllItems();
        for (GatewayEntity newGatewayEntity : newGatewayInfo.getGatewayEntities()) {
            for (GWAttachment attachment : attachmentsMap.values()) {
                if (attachment.getResourceId().equals(newGatewayInfo.getResourceId())) {
                    for (GatewayEntity oldGatewayEntity : gatewayEntities) {
                        if (oldGatewayEntity.getId().equals(attachment.getGatewayId()) && oldGatewayEntity.getType().equals(newGatewayEntity.getType())) {
                            oldGatewayEntity.setState(newGatewayEntity.getState());
                        }
                    }
                }
            }
        }

        gatewayRepository.addItem(oldGatewayInfo);
    }

    public void deleteGatewayInfoForZeta(String projectId, String vpcId) throws Exception {
        GatewayInfo gatewayInfo = gatewayRepository.findItem(vpcId);
        Map<String, GWAttachment> attachmentsMap = gwAttachmentRepository.findAllItems();
        gatewayRepository.deleteGatewayInfoForZeta(vpcId,gatewayInfo,attachmentsMap);
    }


    private void createGatewayAndAttachment(VpcInfo vpcInfo, GatewayInfo gatewayInfo, GatewayEntity gatewayEntity) throws CacheException {
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

    private void updateDPMCacheGateway(AsyncExecutor executor, GatewayInfo gatewayInfo, GatewayEntity gatewayEntity, String projectId) {
        List<Object> result = executor.joinAll();
        if (result.size() > 0) {
            for (Object o : result) {
                if (o.getClass().equals(GatewayIpJson.class)) {
                    GatewayIpJson gatewayIpJson = (GatewayIpJson) o;
                    List<GatewayIp> gatewayIps = gatewayIpJson.getGatewayIps();
                    gatewayEntity.setIps(gatewayIps);
                    gatewayEntity.setState("READY");
                    restClinet.updateDPMCacheGateway(projectId, gatewayInfo);
                }
            }
        }
    }


    @Retryable(maxAttempts = 4)
    private void createDPMCacheGateway(AsyncExecutor executor, GatewayInfo gatewayInfo, GatewayEntity gatewayEntity, String projectId) {
        executor.runAsync(restClinet::createDPMCacheGateway, projectId, gatewayInfo);
    }


    @Recover
    private void workAfterRetryFailed(AsyncExecutor executor, GatewayInfo gatewayInfo, GatewayEntity gatewayEntity, String projectId) {
        rollback(executor, gatewayInfo, gatewayEntity, projectId);
    }

    /**
     * rollback if async call failed
     *
     * @param executor
     * @param projectId
     */
    private void rollback(AsyncExecutor executor, GatewayInfo gatewayInfo, GatewayEntity gatewayEntity, String projectId) {
        List<Object> result = executor.joinAll();
        log.info("start rollback and update the state for GatewayEntity, the executor's result is: {}", result);
        for (Object ob : result) {
            if (ob.getClass().equals(GatewayIpJson.class)) {
                restClinet.deleteVPCInZetaGateway(((GatewayIpJson) ob).getVpcId());
                result.remove(ob);
            }
            if (ob.getClass().equals(String.class)) {
                gatewayEntity.setState("FAILED");
                restClinet.updateDPMCacheGateway(projectId, gatewayInfo);
                result.remove(ob);
            }
        }
    }
}
