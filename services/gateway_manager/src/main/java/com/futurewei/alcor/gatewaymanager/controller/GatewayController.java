package com.futurewei.alcor.gatewaymanager.controller;


import com.futurewei.alcor.common.utils.RestPreconditionsUtil;
import com.futurewei.alcor.gatewaymanager.config.ExceptionMsgConfig;
import com.futurewei.alcor.gatewaymanager.entity.GatewayEntity;
import com.futurewei.alcor.gatewaymanager.entity.GatewayInfo;
import com.futurewei.alcor.gatewaymanager.entity.GatewayWebJson;
import com.futurewei.alcor.gatewaymanager.entity.VpcInfo;
import com.futurewei.alcor.gatewaymanager.service.GatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GatewayController {

    @Autowired
    private GatewayService gatewayService;

    /**
     * List VPC’s Available Gateways
     *
     * @param projectId
     * @param vpcId
     * @return
     */
    @GetMapping("/project/{projectid}/vpcs/{vpc_id}/gateways")
    public GatewayWebJson getGatewaysByVpcId(@PathVariable String projectId, @PathVariable String vpcId) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
        List<GatewayEntity> gateways = gatewayService.getGateways(vpcId);
        return new GatewayWebJson(gateways);
    }

    /**
     * Register a VPC
     *
     * @param projectId
     * @param vpcId
     */
    @PostMapping("/project/{projectid}/vpcs/{vpc_id}/gateway")
    public void createGatewayInfo(@PathVariable String projectId, @PathVariable String vpcId, @RequestBody VpcInfo vpcInfo) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
        gatewayService.createGatewayInfo(vpcInfo);
    }

    /**
     * Query gateway’s state
     *
     * @param projectId
     * @param vpcId
     * @param gatewayId
     * @return
     */
    @GetMapping("/project/{projectid}/vpcs/{vpc_id}/gateway/{gateway_id}")
    public GatewayEntity getGatewayStateById(@PathVariable String projectId, @PathVariable String vpcId, @PathVariable String gatewayId) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(gatewayId);
        List<GatewayEntity> gateways = gatewayService.getGateways(vpcId);
        GatewayEntity gateway = gateways.stream().filter(gatewayEntity -> gatewayEntity.getId().equals(gatewayId)).findFirst().orElse(null);
        if (gateway == null) {
            throw new Exception(ExceptionMsgConfig.GATEWAY_ENTITY_NOT_FOUND.getMsg());
        }
        return gateway;
    }

    /**
     * Update gatewayInfo (for zeta gateway)
     *
     * @param projectId
     * @param vpcId
     * @return
     */
    @PutMapping("/project/{projectid}/gatewayinfo/{resource_id}")
    public GatewayInfo updateGatewayInfoForZeta(@PathVariable String projectId, @PathVariable String vpcId, @RequestBody GatewayInfo gatewayInfo) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
        gatewayService.updateGatewayInfoForZeta(gatewayInfo);
        return null;
    }

    /**
     * Update a gateway by ID
     *
     * @param projectId
     * @param vpcId
     * @param gatewayId
     * @return
     */
    @PutMapping("/project/{projectid}/vpcs/{vpc_id}/gateway/{gateway_id}")
    public GatewayWebJson updateGatewayById(@PathVariable String projectId, @PathVariable String vpcId, @PathVariable String gatewayId) {

        return null;
    }

    /**
     * Delete a gateway
     *
     * @param projectId
     * @param vpcId
     * @param gatewayId
     */
    @DeleteMapping("/project/{projectid}/vpcs/{vpc_id}/gateway/{gateway_id}")
    public void deleteGatewayById(@PathVariable String projectId, @PathVariable String vpcId, @PathVariable String gatewayId) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(gatewayId);
        gatewayService.deleteGateway(vpcId, gatewayId);
    }
}
