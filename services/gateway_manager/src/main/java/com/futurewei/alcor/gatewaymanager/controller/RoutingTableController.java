/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

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
    @PostMapping("/project/{project_id}/gateways/{gateway_id}/routetables")
    public void createRouteTable(@PathVariable("project_id") String projectId, @PathVariable("gateway_id") String gatewayId, @RequestBody RouteTable routeTable) {
        routeTableService.createRouteTable();
    }

    /**
     * List all routing tables
     *
     * @param projectId
     * @param gatewayId
     */
    @GetMapping("/project/{project_id}/gateways/{gateway_id}/routetables")
    public void getAllRouteTable(@PathVariable("project_id") String projectId, @PathVariable("gateway_id") String gatewayId) {
        routeTableService.getAllRouteTable();
    }

    /**
     * List a routing table
     *
     * @param projectId
     * @param gatewayId
     * @param routetableId
     */
    @GetMapping("/project/{project_id}/gateways/{gateway_id}/routetables/{routetable_id}")
    public void queryRouteTable(@PathVariable("project_id") String projectId, @PathVariable("gateway_id") String gatewayId, @PathVariable("routetable_id") String routetableId) {
        routeTableService.queryRouteTable();
    }

    /**
     * Update a routing table
     *
     * @param projectId
     * @param gatewayId
     * @param routetableId
     */
    @PutMapping("/project/{project_id}/gateways/{gateway_id}/routetable/{routetable_id}")
    public void updateRouteTable(@PathVariable("project_id") String projectId, @PathVariable("gateway_id") String gatewayId, @PathVariable("routetable_id") String routetableId) {
        routeTableService.updateRouteTable();
    }

    /**
     * Delete a routing table
     *
     * @param projectId
     * @param gatewayId
     * @param routetableId
     */
    @DeleteMapping("/project/{project_id}/gateways/{gateway_id}/routetable/{routetable_id}")
    public void deleteRouteTable(@PathVariable("project_id") String projectId, @PathVariable("gateway_id") String gatewayId, @PathVariable("routetable_id") String routetableId) {
        routeTableService.deleteRouteTable();
    }

    /**
     * Associate a routing table
     *
     * @param projectId
     * @param gatewayId
     * @param routetableId
     */
    @PutMapping("/project/{project_id}/gateways/{gateway_id}/routetable/{routetable_id}/associate")
    public void associateRouteTable(@PathVariable("project_id") String projectId, @PathVariable("gateway_id") String gatewayId, @PathVariable("routetable_id") String routetableId) {
        routeTableService.associateRouteTable();
    }

    /**
     * De-associate a routing table
     *
     * @param projectId
     * @param gatewayId
     * @param routetableId
     */
    @PutMapping("/project/{project_id}/gateways/{gateway_id}/routetable/{routetable_id}/de-associate")
    public void deAssociateRouteTable(@PathVariable("project_id") String projectId, @PathVariable("gateway_id") String gatewayId, @PathVariable("routetable_id") String routetableId) {
        routeTableService.deleteAssociateRouteTable();
    }
}
