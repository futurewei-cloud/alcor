package com.futurewei.alcor.gatewaymanager.controller;

import com.futurewei.alcor.common.utils.RestPreconditionsUtil;
import com.futurewei.alcor.gatewaymanager.entity.GWAttachment;
import com.futurewei.alcor.gatewaymanager.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;


    /**
     * Create an attachment
     *
     * @param projectId
     * @param gatewayId
     * @param gwAttachment
     */
    @PostMapping("/project/{project_id}/gateways/{gateway_id}/attachments")
    public void createAttachments(@PathVariable("project_id") String projectId, @PathVariable("gateway_id") String gatewayId, @RequestBody GWAttachment gwAttachment) {
        attachmentService.createAttachments();
    }

    /**
     * Update an attachment
     *
     * @param projectId
     * @param gatewayId
     * @param attachId
     */
    @PutMapping("/project/{project_id}/gateways/{gateway_id}/attachments/{attach_id}")
    public void updateAttachments(@PathVariable("project_id") String projectId, @PathVariable("gateway_id") String gatewayId, @PathVariable("attach_id") String attachId) {
        attachmentService.updateAttachments();
    }

    /**
     * Remove an attachment
     *
     * @param projectId
     * @param gatewayId
     * @param attachId
     */
    @DeleteMapping("/project/{project_id}/gateways/{gateway_id}/attachments/{attach_id}")
    public void removeAttachments(@PathVariable("project_id") String projectId, @PathVariable("gateway_id") String gatewayId, @PathVariable("attach_id") String attachId) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(gatewayId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(attachId);
        attachmentService.removeAttachments(gatewayId, attachId);
    }

    /**
     * List all attachments
     *
     * @param projectId
     * @param gatewayId
     */
    @GetMapping("/project/{project_id}/gateways/{gateway_id}/attachments")
    public List<GWAttachment> getAllAttachments(@PathVariable("project_id") String projectId, @PathVariable("gateway_id") String gatewayId) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(gatewayId);
        return attachmentService.getAllAttachments(gatewayId);
    }

    /**
     * Query an attachment
     *
     * @param projectId
     * @param gatewayId
     * @param attachId
     */
    @GetMapping("/project/{project_id}/gateways/{gateway_id}/attachments/{attach_id}")
    public GWAttachment queryAttachments(@PathVariable("project_id") String projectId, @PathVariable("gateway_id") String gatewayId, @PathVariable("attach_id") String attachId) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(gatewayId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(attachId);
        return attachmentService.queryAttachments(gatewayId, attachId);
    }
}
