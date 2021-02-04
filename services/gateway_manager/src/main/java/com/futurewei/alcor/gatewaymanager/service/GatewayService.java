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
import com.futurewei.alcor.web.restclient.GatewayManagerRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Supplier;

@Slf4j
@Service
@ComponentScan("com.futurewei.alcor.web.restclient")
public class GatewayService {

    @Autowired
    private GatewayRepository gatewayRepository;

    @Autowired
    private GWAttachmentRepository gwAttachmentRepository;

    @Autowired
    private GatewayManagerRestClient restClient;

    private static final String GATEWAY_DESCRIPTION = "internal gateway";
    private static final String GATEWAY_SUFFIX_NAME = " gateway";
    private static final String ATTACHMENT_NAME_PREFIX = "attachment - ";


    public GatewayService() {
    }

    public GatewayInfo createGatewayInfo(String projectId, VpcInfo vpcInfo) throws Exception {

        //TODO check whether the project type is zeta by projectId

        // construct the GatewayEntity and Attachment
        GatewayInfo gatewayInfo = new GatewayInfo();
        GatewayEntity gatewayEntity = new GatewayEntity();
        createGatewayAndAttachment(projectId, vpcInfo, gatewayInfo, gatewayEntity);

        AsyncExecutor executor = new AsyncExecutor();
        executor.runAsync(restClient::createDPMCacheGateway, projectId, gatewayInfo);
        executor.runAsync(restClient::createVPCInZetaGateway, new VpcInfoSub(vpcInfo.getVpcId(), vpcInfo.getVpcVni()));
        executor.runAsync((Supplier<Void>) () -> {
            // wait result of all async
            List<Object> result = executor.joinAllAsync();
            // whether to rollback by checking result's size is 2
            try {
                updateDPMCacheGateway(result, gatewayInfo, gatewayEntity, projectId);
            } catch (Exception e) {
                log.info("update GatewayEntity status to READY failed, detail message: {}", e.getMessage());
                // rollback GM's DB and ask Zeta to remove the corresponding gateway resources
            }

            try {
                rollback(result, gatewayInfo, gatewayEntity, projectId);
            } catch (Exception e) {
                log.info("rollback failed, detail message: {}", e.getMessage());
                // If rollback failed, we should raise an alarm or error.
            }
            return null;
        });
        return gatewayInfo;
    }

    public void updateGatewayInfoForZeta(String projectId, String vpcId, GatewayInfo newGatewayInfo) throws Exception {
        //TODO check whether the project type is zeta by projectId

        GatewayEntity gatewayEntity = null;
        Map<String, Object[]> queryParams = new HashMap<>();
        queryParams.put("resourceId", new String[]{vpcId});
        Map<String, GWAttachment> attachmentsMap = gwAttachmentRepository.findAllItems(queryParams);

        for (GatewayEntity newGatewayEntity : newGatewayInfo.getGatewayEntities()) {
            for (GWAttachment attachment : attachmentsMap.values()) {
                gatewayEntity = gatewayRepository.findItem(attachment.getGatewayId());
                if (gatewayEntity == null) {
                    throw new Exception(ExceptionMsgConfig.GATEWAY_ENTITY_NOT_FOUND.getMsg());
                }
                if (gatewayEntity.getType().equals(newGatewayEntity.getType())) {
                    gatewayEntity.setStatus(newGatewayEntity.getStatus());
                    gatewayRepository.addItem(gatewayEntity);
                }
            }
        }
    }

    public void deleteGatewayInfoForZeta(String projectId, String vpcId) throws Exception {
        //TODO check whether the project type is zeta by projectId

        Map<String, Object[]> queryParams = new HashMap<>();
        queryParams.put("resourceId", new String[]{vpcId});
        Map<String, GWAttachment> attachmentsMap = gwAttachmentRepository.findAllItems(queryParams);
        gatewayRepository.deleteGatewayInfoForZeta(attachmentsMap);

        AsyncExecutor executor = new AsyncExecutor();
        executor.runAsync((Supplier<Void>) () -> {
            try {
                // Notify Zeta Management Plane to delete the vpc
                restClient.deleteVPCInZetaGateway(vpcId);
            } catch (Exception e) {
                e.printStackTrace();
                log.info("delete VPC in ZetaGateway failed,error message is: {}", e.getMessage());
            }
            return null;
        });
        executor.runAsync((Supplier<Void>) () -> {
            try {
                // Notify DPM to delete GatewayInfo in the DPM cache
                restClient.deleteDPMCacheGateway(projectId,vpcId);
            } catch (Exception e) {
                e.printStackTrace();
                log.info("delete GatewayInfo in DPM cache failed,error message is: {}", e.getMessage());
            }
            return null;
        });
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

    private void updateDPMCacheGateway(List<Object> result, GatewayInfo gatewayInfo, GatewayEntity gatewayEntity, String projectId) throws Exception {
        if (result != null && result.size() == 2) {
            log.info("wait for all future result, result: {}", result);
            for (Object o : result) {
                if (o.getClass().equals(ZetaGatewayIpJson.class)) {
                    ZetaGatewayIpJson gatewayIpJson = (ZetaGatewayIpJson) o;
                    List<GatewayIp> gatewayIps = gatewayIpJson.getGatewayIps();
                    gatewayEntity.setIps(gatewayIps);
                    gatewayEntity.setStatus(StatusEnum.READY.getStatus());
                    restClient.updateDPMCacheGateway(projectId, gatewayInfo);
                    gatewayRepository.addItem(gatewayEntity);
                    log.info("update GatewayEntity status to READY success");
                }
            }
        }
    }

    /**
     * rollback if async call failed
     *
     * @param result
     * @param projectId
     */
    private void rollback(List<Object> result, GatewayInfo gatewayInfo, GatewayEntity gatewayEntity, String projectId) throws Exception {
        if (result != null && result.size() != 2) {
            log.info("start rollback and update the status for GatewayEntity, the executor's result is: {}", result);
            Iterator<Object> iterator = result.iterator();
            while (iterator.hasNext()) {
                Object ob = iterator.next();
                if (ob.getClass().equals(ZetaGatewayIpJson.class)) {
                    restClient.deleteVPCInZetaGateway(((ZetaGatewayIpJson) ob).getVpcId());
                    iterator.remove();
                }
                if (ob.getClass().equals(String.class)) {
                    gatewayEntity.setStatus(StatusEnum.FAILED.getStatus());
                    restClient.updateDPMCacheGateway(projectId, gatewayInfo);
                    gatewayRepository.addItem(gatewayEntity);
                    iterator.remove();
                }
            }
        }
    }
}
