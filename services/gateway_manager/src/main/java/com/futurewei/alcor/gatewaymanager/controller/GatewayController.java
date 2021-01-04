package com.futurewei.alcor.gatewaymanager.controller;


import com.futurewei.alcor.gatewaymanager.entity.GatewayWebJson;
import org.springframework.web.bind.annotation.*;

@RestController
public class GatewayController {

    /**
     * List VPC’s Available Gateways
     *
     * @param projectId
     * @param vpcId
     * @return
     */
    @GetMapping("/project/{projectid}/vpcs/{vpc_id}/gateways")
    public GatewayWebJson getGatewaysByVpcId(@PathVariable String projectId, @PathVariable String vpcId) {

        return null;
    }

    /**
     * Register a VPC
     *
     * @param projectId
     * @param vpcId
     */
    @PostMapping("/project/{projectid}/vpcs/{vpc_id}/gateway")
    public void createGatewayByVpcId(@PathVariable String projectId, @PathVariable String vpcId) {

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
    public String getGatewayStateById(@PathVariable String projectId, @PathVariable String vpcId, @PathVariable String gatewayId) {

        return null;
    }

    /**
     * Update a gateway
     *
     * @param projectId
     * @param vpcId
     * @return
     */
    @PutMapping("/project/{projectid}/vpcs/{vpc_id}/gateway")
    public GatewayWebJson updateGatewayByVpcId(@PathVariable String projectId, @PathVariable String vpcId) {

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
    public void deleteGatewayById(@PathVariable String projectId, @PathVariable String vpcId, @PathVariable String gatewayId) {

    }
}
