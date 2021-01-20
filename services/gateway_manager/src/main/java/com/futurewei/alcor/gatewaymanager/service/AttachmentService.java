package com.futurewei.alcor.gatewaymanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.gatewaymanager.config.ExceptionMsgConfig;
import com.futurewei.alcor.gatewaymanager.dao.GWAttachmentRepository;
import com.futurewei.alcor.gatewaymanager.dao.GatewayRepository;
import com.futurewei.alcor.gatewaymanager.entity.GWAttachment;
import com.futurewei.alcor.web.entity.gateway.GatewayEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AttachmentService {

    @Autowired
    private GWAttachmentRepository gwAttachmentRepository;

    @Autowired
    private GatewayRepository gatewayRepository;

    public void createAttachments() {
    }

    public void updateAttachments() {
    }


    public void removeAttachments(String gatewayId, String attachId) throws Exception {
        GWAttachment attachment = gwAttachmentRepository.findItem(attachId);
        if (attachment == null) {
            throw new Exception(ExceptionMsgConfig.ATTACHMENT_NOT_FOUND.getMsg());
        }
        GatewayEntity gatewayEntity = gatewayRepository.findItem(gatewayId);
        if (gatewayEntity == null) {
            throw new Exception(ExceptionMsgConfig.GATEWAY_ENTITY_NOT_FOUND.getMsg());
        }
        if (!gatewayId.equals(attachment.getGatewayId())) {
            throw new Exception(ExceptionMsgConfig.GATEWAY_NOT_ASSOCIATED_ATTACHMENT.getMsg());
        }
        gatewayEntity.getAttachments().removeIf(attachId::equals);

        //delete attachment and update GatewayEntity in the DB
        gwAttachmentRepository.deleteAndUpdateItem(attachId,gatewayEntity);
    }


    public List<GWAttachment> getAllAttachments(String gatewayId) throws CacheException {
        Map<String, Object[]> queryParams = new HashMap<>();
        queryParams.put("gatewayId", new String[]{gatewayId});
        Map<String, GWAttachment> attachmentMap = gwAttachmentRepository.findAllItems(queryParams);
        return (List<GWAttachment>) attachmentMap.values();
    }


    public GWAttachment queryAttachments(String gatewayId, String attachId) throws CacheException {
        return gwAttachmentRepository.findItem(attachId);
    }

}
