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
    public void createRouteTable(@PathVariable String projectId, @PathVariable String gatewayId, @RequestBody RouteTable routeTable) {
        routeTableService.createRouteTable();
    }

    /**
     * List all routing tables
     *
     * @param projectId
     * @param gatewayId
     */
    @GetMapping("/project/{projectid}/gateways/{gateway_id}/routetables")
    public void getAllRouteTable(@PathVariable String projectId, @PathVariable String gatewayId) {
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
    public void queryRouteTable(@PathVariable String projectId, @PathVariable String gatewayId,@PathVariable String routetableId) {
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
    public void updateRouteTable(@PathVariable String projectId, @PathVariable String gatewayId,@PathVariable String routetableId) {
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
    public void deleteRouteTable(@PathVariable String projectId, @PathVariable String gatewayId,@PathVariable String routetableId) {
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
    public void associateRouteTable(@PathVariable String projectId, @PathVariable String gatewayId,@PathVariable String routetableId) {
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
    public void deAssociateRouteTable(@PathVariable String projectId, @PathVariable String gatewayId,@PathVariable String routetableId) {
        routeTableService.deleteAssociateRouteTable();
    }
}
