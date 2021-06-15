/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.route.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.route.exception.*;
import com.futurewei.alcor.web.entity.route.*;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

import java.util.List;

public interface RouterService {

    public Router getOrCreateVpcRouter (String projectId, String vpcId) throws CanNotFindVpc, DatabasePersistenceException, CacheException, OwnMultipleVpcRouterException;
    public Router createDefaultVpcRouter (String projectId, VpcEntity vpcEntity) throws DatabasePersistenceException;
    public String deleteVpcRouter (String projectId, String vpcId) throws Exception;
    public RouteTable getOrCreateVpcRouteTable (String projectId, String vpcId) throws DatabasePersistenceException, CanNotFindVpc, CacheException, OwnMultipleVpcRouterException;
    public RouteTable createDefaultVpcRouteTable (String projectId, Router router) throws DatabasePersistenceException;
    public RouteTable updateVpcRouteTable (String projectId, String vpcId, RouteTableWebJson resource) throws DatabasePersistenceException, CanNotFindVpc, CacheException, OwnMultipleVpcRouterException, ResourceNotFoundException, ResourcePersistenceException;
    public List<RouteTable> getVpcRouteTables (String projectId, String vpcId) throws CanNotFindVpc;
    public RouteTable getSubnetRouteTable(String projectId, String subnetId) throws CanNotFindSubnet, CacheException, OwnMultipleSubnetRouteTablesException, DatabasePersistenceException, ResourceNotFoundException, ResourcePersistenceException, OwnMultipleVpcRouterException, CanNotFindVpc;
    public RouteTable updateSubnetRouteTable (String projectId, String subnetId, UpdateRoutingRuleResponse resource) throws CacheException, DatabasePersistenceException, OwnMultipleSubnetRouteTablesException, CanNotFindVpc, CanNotFindSubnet, ResourceNotFoundException, ResourcePersistenceException, OwnMultipleVpcRouterException;
    public String deleteSubnetRouteTable (String projectId, String subnetId) throws Exception;
    public RouteTable createNeutronSubnetRouteTable(String projectId, String subnetId, RouteTableWebJson resource, List<RouteEntry> routes) throws DatabasePersistenceException;

}
