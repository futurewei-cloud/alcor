package com.futurewei.alcor.gatewaymanager.controller;


import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.utils.RestPreconditionsUtil;
import com.futurewei.alcor.gatewaymanager.config.ExceptionMsgConfig;
import com.futurewei.alcor.gatewaymanager.entity.GatewayWebJson;
import com.futurewei.alcor.gatewaymanager.service.GatewayService;
import com.futurewei.alcor.web.entity.gateway.GatewayEntity;
import com.futurewei.alcor.web.entity.gateway.GatewayInfo;
import com.futurewei.alcor.web.entity.gateway.VpcInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public GatewayWebJson getGatewaysByVpcId(@PathVariable("projectid") String projectId, @PathVariable("vpc_id") String vpcId) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
        List<GatewayEntity> gateways = gatewayService.getGateways(vpcId);
        return new GatewayWebJson(gateways);
    }

    /**
     * Create a GatewayInfo (for zeta gateway)
     *
     * @param projectId
     */
    @PostMapping("/project/{projectid}/gatewayinfo")
    @ResponseStatus(HttpStatus.CREATED)
    public void createGatewayInfo(@PathVariable("projectid") String projectId, @RequestBody VpcInfo vpcInfo) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        gatewayService.createGatewayInfo(projectId, vpcInfo);
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
    public GatewayEntity getGatewayStateById(@PathVariable("projectid") String projectId, @PathVariable("vpc_id") String vpcId, @PathVariable("gateway_id") String gatewayId) throws Exception {
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseId updateGatewayInfoForZeta(@PathVariable("projectid") String projectId, @PathVariable("resource_id") String vpcId, @RequestBody GatewayInfo gatewayInfo) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
        gatewayService.updateGatewayInfoForZeta(projectId, gatewayInfo);
        return new ResponseId(vpcId);
    }

    /**
     * Delete a GatewayInfo (for zeta gateway)
     *
     * @param projectId
     * @param vpcId
     * @return
     * @throws Exception
     */
    @DeleteMapping("/project/{projectid}/gatewayinfo/{resource_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseId deleteGatewayInfoForZeta(@PathVariable("projectid") String projectId, @PathVariable("resource_id") String vpcId) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
        gatewayService.deleteGatewayInfoForZeta(projectId, vpcId);
        return new ResponseId(vpcId);
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
    public GatewayWebJson updateGatewayById(@PathVariable("projectid") String projectId, @PathVariable("vpc_id") String vpcId, @PathVariable("gateway_id") String gatewayId) {
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
    public void deleteGatewayById(@PathVariable("projectid") String projectId, @PathVariable("vpc_id") String vpcId, @PathVariable("gateway_id") String gatewayId) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(gatewayId);
        gatewayService.deleteGateway(vpcId, gatewayId);
    }
}
