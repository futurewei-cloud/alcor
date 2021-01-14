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

        //TODO check whether the project type is zeta by projectId

        // create GatewayInfo entity in the DB
        GatewayInfo gatewayInfo = new GatewayInfo();
        GatewayEntity gatewayEntity = new GatewayEntity();

        // construct the GatewayEntity and Attachment
        createGatewayAndAttachment(projectId, vpcInfo, gatewayInfo, gatewayEntity);

        AsyncExecutor executor = new AsyncExecutor();
        createDPMCacheGateway(executor, gatewayInfo, gatewayEntity, projectId);
        try {
            executor.runAsync(restClinet::createVPCInZetaGateway, new VpcInfoSub(vpcInfo.getVpcId(), vpcInfo.getVpcVni()));
        } catch (CompletionException e) {
            log.info("failed to create vpc in the Gateway, Exception detail: {}", e.getMessage());
            rollback(executor, gatewayInfo, gatewayEntity, projectId);
            //TODO if rollback failed,how we should handle it?
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
        //TODO check whether the project type is zeta by projectId

        GatewayEntity gatewayEntity = null;
        Map<String, GWAttachment> attachmentsMap = gwAttachmentRepository.findAllItems();
        for (GatewayEntity newGatewayEntity : newGatewayInfo.getGatewayEntities()) {
            for (GWAttachment attachment : attachmentsMap.values()) {
                if (attachment.getResourceId().equals(newGatewayInfo.getResourceId())) {
                    gatewayEntity = gatewayRepository.findItem(attachment.getGatewayId());
                    if (gatewayEntity == null) {
                        throw new Exception(ExceptionMsgConfig.GATEWAY_ENTITY_NOT_FOUND.getMsg());
                    }
                    if (gatewayEntity.getType().equals(newGatewayEntity.getType())) {
                        gatewayEntity.setStatus(newGatewayEntity.getStatus());
                    }
                }
            }
        }
        if (gatewayEntity != null) {
            gatewayRepository.addItem(gatewayEntity);
        }
    }

    public void deleteGatewayInfoForZeta(String projectId, String vpcId) throws Exception {
        //TODO check whether the project type is zeta by projectId

        Map<String, GWAttachment> attachmentsMap = gwAttachmentRepository.findAllItems();
        gatewayRepository.deleteGatewayInfoForZeta(vpcId, attachmentsMap);
    }


    private void createGatewayAndAttachment(String projectId, VpcInfo vpcInfo, GatewayInfo gatewayInfo, GatewayEntity gatewayEntity) throws Exception {
        gatewayEntity.setProjectId(projectId);
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
        // create attachment and associated to the GatewayEntity
        GWAttachment attachment = new GWAttachment(GatewayType.ZETA.getGatewayType() + ATTACHMENT_NAME_PREFIX + ResourceType.VPC.name(),
                ResourceType.VPC, vpcInfo.getVpcId(), gatewayEntity.getId(), StatusEnum.AVAILABLE.getStatus(), vpcInfo.getVpcVni());
        ArrayList<String> attachmentIds = new ArrayList<>();
        attachmentIds.add(attachment.getId());
        gatewayEntity.setAttachments(attachmentIds);

        // store in the GM's DB Cache
        gatewayRepository.addGatewayAndAttachment(gatewayEntity, attachment);
    }

    public void deleteGatewayById(String gatewayId) throws Exception {
        GatewayEntity gatewayEntity = gatewayRepository.findItem(gatewayId);
        if (gatewayEntity == null) {
            throw new Exception(ExceptionMsgConfig.GATEWAY_ENTITY_NOT_FOUND.getMsg());
        }
        gatewayRepository.deleteItem(gatewayId);
    }


    public GatewayEntity getGatewayEntityById(String gatewayId) throws CacheException {
        return gatewayRepository.findItem(gatewayId);
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
                    gatewayRepository.addItem(gatewayEntity);
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
