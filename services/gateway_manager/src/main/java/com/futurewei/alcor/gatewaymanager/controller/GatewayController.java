package com.futurewei.alcor.gatewaymanager.controller;


import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.common.utils.RestPreconditionsUtil;
import com.futurewei.alcor.gatewaymanager.entity.GatewaysWebJson;
import com.futurewei.alcor.gatewaymanager.service.GatewayService;
import com.futurewei.alcor.gatewaymanager.utils.VerifyParameterUtils;
import com.futurewei.alcor.web.entity.gateway.GatewayEntity;
import com.futurewei.alcor.web.entity.gateway.GatewayInfo;
import com.futurewei.alcor.web.entity.gateway.GatewayInfoJson;
import com.futurewei.alcor.web.entity.gateway.VpcInfoJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class GatewayController {

    @Autowired
    private GatewayService gatewayService;

    /**
     * Create a GatewayInfo (for zeta gateway)
     *
     * @param projectId
     */
    @PostMapping("/project/{project_id}/gatewayinfo")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseId createGatewayInfo(@PathVariable("project_id") String projectId, @RequestBody VpcInfoJson vpcInfoJson) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        VerifyParameterUtils.checkVpcInfo(vpcInfoJson.getVpcInfo());
        GatewayInfo gatewayInfo = gatewayService.createGatewayInfo(projectId, vpcInfoJson.getVpcInfo());
        log.info("GatewayInfo created success,GatewayInfo is: {}", gatewayInfo);
        return new ResponseId(gatewayInfo.getResourceId());
    }

    /**
     * Update gatewayInfo (for zeta gateway)
     *
     * @param projectId
     * @param vpcId
     * @return
     */
    @PutMapping("/project/{project_id}/gatewayinfo/{resource_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseId updateGatewayInfoForZeta(@PathVariable("project_id") String projectId, @PathVariable("resource_id") String vpcId, @RequestBody GatewayInfoJson gatewayInfoJson) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
        VerifyParameterUtils.checkGatewayInfo(gatewayInfoJson.getGatewayInfo());
        gatewayService.updateGatewayInfoForZeta(projectId, vpcId, gatewayInfoJson.getGatewayInfo());
        log.info("GatewayInfo updated success,GatewayInfo is: {}", gatewayInfoJson.getGatewayInfo());
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
    @DeleteMapping("/project/{project_id}/gatewayinfo/{resource_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseId deleteGatewayInfoForZeta(@PathVariable("project_id") String projectId, @PathVariable("resource_id") String vpcId) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcId);
        gatewayService.deleteGatewayInfoForZeta(projectId, vpcId);
        log.info("GatewayInfo deleted success,the resource_id is: {}", vpcId);
        return new ResponseId(vpcId);
    }


    /**
     * Create a gateway
     *
     * @param projectId
     */
    @PostMapping("/project/{project_id}/gateways")
    @ResponseStatus(HttpStatus.CREATED)
    public void createGateway(@PathVariable("project_id") String projectId) {

    }

    /**
     * Update a gateway
     *
     * @param projectId
     */
    @PutMapping("/project/{project_id}/gateways")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateGateway(@PathVariable("project_id") String projectId) {

    }

    /**
     * Update a gateway by ID
     *
     * @param projectId
     * @param gatewayId
     * @return
     */
    @PutMapping("/project/{project_id}/gateways/{gateway_id}")
    public void updateGatewayById(@PathVariable("project_id") String projectId, @PathVariable("gateway_id") String gatewayId) {

    }

    /**
     * Query a gateway’s state
     *
     * @param projectId
     * @param gatewayId
     * @return
     */
    @GetMapping("/project/{project_id}/gateways/{gateway_id}")
    public GatewayEntity getGatewayStateById(@PathVariable("project_id") String projectId, @PathVariable("gateway_id") String gatewayId) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(gatewayId);
        return gatewayService.getGatewayEntityById(gatewayId);
    }

    /**
     * List All Available Gateways
     *
     * @param projectId
     * @return
     */
    @GetMapping("/project/{project_id}/gateways")
    public GatewaysWebJson getGatewaysByVpcId(@PathVariable("project_id") String projectId) {
        return null;
    }

    /**
     * Delete a gateway
     *
     * @param projectId
     * @param gatewayId
     */
    @DeleteMapping("/project/{project_id}/gateways/{gateway_id}")
    public void deleteGatewayById(@PathVariable("project_id") String projectId, @PathVariable("gateway_id") String gatewayId) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(gatewayId);
        gatewayService.deleteGatewayById(gatewayId);
    }


}
