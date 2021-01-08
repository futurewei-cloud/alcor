package com.futurewei.alcor.gatewaymanager.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.gatewaymanager.config.ExceptionMsgConfig;
import com.futurewei.alcor.gatewaymanager.dao.GWAttachmentRepository;
import com.futurewei.alcor.gatewaymanager.dao.GatewayRepository;
import com.futurewei.alcor.gatewaymanager.entity.GWAttachment;
import com.futurewei.alcor.gatewaymanager.entity.GatewayInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        if (!gatewayId.equals(attachment.getGatewayId())) {
            throw new Exception(ExceptionMsgConfig.GATEWAY_NOT_ASSOCIATED_ATTACHMENT.getMsg());
        }
        String resourceId = attachment.getResourceId();
        GatewayInfo gatewayInfo = gatewayRepository.findItem(resourceId);
        gatewayInfo.getGatewayEntities().stream().filter(gatewayEntity -> gatewayId.equals(gatewayEntity.getId()))
                .forEach(gatewayEntity -> gatewayEntity.getAttachments().removeIf(attachId::equals));
        //delete attachment and update GatewayInfo in the DB
        gwAttachmentRepository.deleteItem(attachId,gatewayInfo);
    }


    public List<GWAttachment> getAllAttachments(String gatewayId) throws CacheException {
        Map<String, GWAttachment> attachmentMap = gwAttachmentRepository.findAllItems();
        return attachmentMap.values().stream().filter(attachment -> gatewayId.equals(attachment.getGatewayId()))
                .collect(Collectors.toCollection(ArrayList::new));
    }


    public GWAttachment queryAttachments(String gatewayId, String attachId) throws CacheException {
        return gwAttachmentRepository.findItem(attachId);
    }

}
