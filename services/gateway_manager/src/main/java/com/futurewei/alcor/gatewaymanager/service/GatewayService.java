package com.futurewei.alcor.gatewaymanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.executor.AsyncExecutor;
import com.futurewei.alcor.gatewaymanager.config.ExceptionMsgConfig;
import com.futurewei.alcor.gatewaymanager.dao.GWAttachmentRepository;
import com.futurewei.alcor.gatewaymanager.dao.GatewayRepository;
import com.futurewei.alcor.gatewaymanager.entity.GWAttachment;
import com.futurewei.alcor.gatewaymanager.entity.ResourceType;
import com.futurewei.alcor.gatewaymanager.entity.StatusEnum;
import com.futurewei.alcor.web.entity.gateway.*;
import com.futurewei.alcor.web.restclient.GatewayManagerRestClinet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

@Slf4j
@EnableRetry
@Service
@Component("com.futurewei.alcor.web.restclient")
public class GatewayService {

    @Autowired
    private GatewayRepository gatewayRepository;

    @Autowired
    private GWAttachmentRepository gwAttachmentRepository;

    @Autowired
    private GatewayManagerRestClinet restClinet;

    private static final String GATEWAY_DESCRIPTION = "internal gateway";
    private static final String GATEWAY_SUFFIX_NAME = " name";
    private static final String ATTACHMENT_NAME_PREFIX = "attachment - ";


    public GatewayService() {
    }

    public GatewayInfo createGatewayInfo(String projectId, VpcInfo vpcInfo) throws Exception {

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
        executor.runAsync((Supplier<Void>) () -> {
            try {
                updateDPMCacheGateway(executor, gatewayInfo, gatewayEntity, projectId);
            } catch (Exception e) {
                log.info("update GatewayEntity status to READY failed, detail message: {}", e.getMessage());
                //TODO how to handle this exception? rollback all?
            }
            return null;
        });
        return gatewayInfo;
    }

    public void updateGatewayInfoForZeta(String projectId, GatewayInfo newGatewayInfo) throws Exception {
        GatewayInfo oldGatewayInfo = gatewayRepository.findItem(newGatewayInfo.getResourceId());
        if (oldGatewayInfo == null) {
            throw new Exception(ExceptionMsgConfig.GATEWAYINFO_NOT_FOUND.getMsg());
        }
        List<GatewayEntity> gatewayEntities = oldGatewayInfo.getGatewayEntities();
        if (gatewayEntities == null || gatewayEntities.size() == 0) {
            throw new Exception(ExceptionMsgConfig.GATEWAYS_IS_NULL.getMsg());
        }

        Map<String, GWAttachment> attachmentsMap = gwAttachmentRepository.findAllItems();
        for (GatewayEntity newGatewayEntity : newGatewayInfo.getGatewayEntities()) {
            for (GWAttachment attachment : attachmentsMap.values()) {
                if (attachment.getResourceId().equals(newGatewayInfo.getResourceId())) {
                    for (GatewayEntity oldGatewayEntity : gatewayEntities) {
                        if (oldGatewayEntity.getId().equals(attachment.getGatewayId()) && oldGatewayEntity.getType().equals(newGatewayEntity.getType())) {
                            oldGatewayEntity.setStatus(newGatewayEntity.getStatus());
                        }
                    }
                }
            }
        }

        gatewayRepository.addItem(oldGatewayInfo);
    }

    public void deleteGatewayInfoForZeta(String projectId, String vpcId) throws Exception {
        GatewayInfo gatewayInfo = gatewayRepository.findItem(vpcId);
        if (gatewayInfo == null) {
            throw new Exception(ExceptionMsgConfig.GATEWAYINFO_NOT_FOUND.getMsg());
        }
        Map<String, GWAttachment> attachmentsMap = gwAttachmentRepository.findAllItems();
        gatewayRepository.deleteGatewayInfoForZeta(vpcId, gatewayInfo, attachmentsMap);
    }


    private void createGatewayAndAttachment(VpcInfo vpcInfo, GatewayInfo gatewayInfo, GatewayEntity gatewayEntity) throws CacheException {
        gatewayEntity.setId(UUID.randomUUID().toString());
        gatewayEntity.setDescription(GATEWAY_DESCRIPTION);
        gatewayEntity.setType(GatewayType.ZETA);
        gatewayEntity.setName(gatewayEntity.getType() + GATEWAY_SUFFIX_NAME);
        gatewayEntity.setStatus(StatusEnum.PENDING.getStatus());
        gatewayInfo.setResourceId(vpcInfo.getVpcId());
        ArrayList<GatewayEntity> gatewayEntities = new ArrayList<>();
        gatewayEntities.add(gatewayEntity);
        gatewayInfo.setGatewayEntities(gatewayEntities);
        gatewayInfo.setStatus(StatusEnum.AVAILABLE.getStatus());
        // create attachment and attach it to the GatewayEntity
        GWAttachment attachment = new GWAttachment(GatewayType.ZETA.getGatewayType() + ATTACHMENT_NAME_PREFIX + ResourceType.VPC.name(),
                ResourceType.VPC, vpcInfo.getVpcId(), gatewayEntity.getId(), StatusEnum.AVAILABLE.getStatus(), vpcInfo.getVpcVni());
        ArrayList<String> attachmentIds = new ArrayList<>();
        attachmentIds.add(attachment.getId());
        gatewayEntity.setAttachments(attachmentIds);

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

    private void updateDPMCacheGateway(AsyncExecutor executor, GatewayInfo gatewayInfo, GatewayEntity gatewayEntity, String projectId) throws Exception {
        List<Object> result = executor.joinAll();
        log.info("wait for all future result, result: {}", result);
        if (result.size() > 0) {
            for (Object o : result) {
                if (o.getClass().equals(GatewayIpJson.class)) {
                    GatewayIpJson gatewayIpJson = (GatewayIpJson) o;
                    List<GatewayIp> gatewayIps = gatewayIpJson.getGatewayIps();
                    gatewayEntity.setIps(gatewayIps);
                    gatewayEntity.setStatus(StatusEnum.READY.getStatus());
                    restClinet.updateDPMCacheGateway(projectId, gatewayInfo);
                    gatewayRepository.addItem(gatewayInfo);
                }
            }
        }
    }


    @Retryable(maxAttempts = 4)
    private void createDPMCacheGateway(AsyncExecutor executor, GatewayInfo gatewayInfo, GatewayEntity gatewayEntity, String projectId) {
        executor.runAsync(restClinet::createDPMCacheGateway, projectId, gatewayInfo);
    }


    @Recover
    private void workAfterRetryFailed(AsyncExecutor executor, GatewayInfo gatewayInfo, GatewayEntity gatewayEntity, String projectId) throws Exception {
        rollback(executor, gatewayInfo, gatewayEntity, projectId);
    }

    /**
     * rollback if async call failed
     *
     * @param executor
     * @param projectId
     */
    private void rollback(AsyncExecutor executor, GatewayInfo gatewayInfo, GatewayEntity gatewayEntity, String projectId) throws Exception {
        List<Object> result = executor.joinAll();
        log.info("start rollback and update the status for GatewayEntity, the executor's result is: {}", result);
        Iterator<Object> iterator = result.iterator();
        while (iterator.hasNext()) {
            Object ob = iterator.next();
            if (ob.getClass().equals(GatewayIpJson.class)) {
                restClinet.deleteVPCInZetaGateway(((GatewayIpJson) ob).getVpcId());
                iterator.remove();
            }
            if (ob.getClass().equals(String.class)) {
                gatewayEntity.setStatus(StatusEnum.FAILED.getStatus());
                restClinet.updateDPMCacheGateway(projectId, gatewayInfo);
                iterator.remove();
            }
        }
    }
}
