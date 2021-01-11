package com.futurewei.alcor.gatewaymanager.controller;

import com.futurewei.alcor.gatewaymanager.service.RouteTableService;
import com.futurewei.alcor.web.entity.route.RouteTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class RoutingTableController {

    @Autowired
    private RouteTableService routeTableService;

    /**
     * Create a routing table
     *
     * @param projectId
     * @param gatewayId
     * @param routeTable
     */
    @PostMapping("/project/{projectid}/gateways/{gateway_id}/routetables")
    public void createRouteTable(@PathVariable("projectid") String projectId, @PathVariable("gateway_id") String gatewayId, @RequestBody RouteTable routeTable) {
        routeTableService.createRouteTable();
    }

    /**
     * List all routing tables
     *
     * @param projectId
     * @param gatewayId
     */
    @GetMapping("/project/{projectid}/gateways/{gateway_id}/routetables")
    public void getAllRouteTable(@PathVariable("projectid") String projectId, @PathVariable("gateway_id") String gatewayId) {
        routeTableService.getAllRouteTable();
    }

    /**
     * List a routing table
     *
     * @param projectId
     * @param gatewayId
     * @param routetableId
     */
    @GetMapping("/project/{projectid}/gateways/{gateway_id}/routetables/{routetable_id}")
    public void queryRouteTable(@PathVariable("projectid") String projectId, @PathVariable("gateway_id") String gatewayId, @PathVariable("routetable_id") String routetableId) {
        routeTableService.queryRouteTable();
    }

    /**
     * Update a routing table
     *
     * @param projectId
     * @param gatewayId
     * @param routetableId
     */
    @PutMapping("/project/{projectid}/gateways/{gateway_id}/routetable/{routetable_id}")
    public void updateRouteTable(@PathVariable("projectid") String projectId, @PathVariable("gateway_id") String gatewayId, @PathVariable("routetable_id") String routetableId) {
        routeTableService.updateRouteTable();
    }

    /**
     * Delete a routing table
     *
     * @param projectId
     * @param gatewayId
     * @param routetableId
     */
    @DeleteMapping("/project/{projectid}/gateways/{gateway_id}/routetable/{routetable_id}")
    public void deleteRouteTable(@PathVariable("projectid") String projectId, @PathVariable("gateway_id") String gatewayId, @PathVariable("routetable_id") String routetableId) {
        routeTableService.deleteRouteTable();
    }

    /**
     * Associate a routing table
     *
     * @param projectId
     * @param gatewayId
     * @param routetableId
     */
    @PutMapping("/project/{projectid}/gateways/{gateway_id}/routetable/{routetable_id}/associate")
    public void associateRouteTable(@PathVariable("projectid") String projectId, @PathVariable("gateway_id") String gatewayId, @PathVariable("routetable_id") String routetableId) {
        routeTableService.associateRouteTable();
    }

    /**
     * De-associate a routing table
     *
     * @param projectId
     * @param gatewayId
     * @param routetableId
     */
    @PutMapping("/project/{projectid}/gateways/{gateway_id}/routetable/{routetable_id}/de-associate")
    public void deAssociateRouteTable(@PathVariable("projectid") String projectId, @PathVariable("gateway_id") String gatewayId, @PathVariable("routetable_id") String routetableId) {
        routeTableService.deleteAssociateRouteTable();
    }
}
