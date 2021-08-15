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
import com.futurewei.alcor.common.exception.QueryParamTypeNotSupportException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.route.exception.*;
import com.futurewei.alcor.web.entity.route.*;

import java.util.List;

public interface NeutronRouterService {

    public NeutronRouterWebRequestObject getNeutronRouter (String routerId) throws ResourceNotFoundException, ResourcePersistenceException, RouterUnavailable;
    public NeutronRouterWebRequestObject saveRouterAndRouterExtraAttribute (NeutronRouterWebRequestObject neutronRouter) throws NeutronRouterIsNull, DatabasePersistenceException;
    public RouterInterfaceResponse addAnInterfaceToNeutronRouter (String projectid, String portId, String subnetId, String routerId) throws SpecifyBothSubnetIDAndPortID, ResourceNotFoundException, ResourcePersistenceException, RouterUnavailable, DatabasePersistenceException, PortIDIsAlreadyExist, PortIsAlreadyInUse, SubnetNotBindUniquePortId, RouterHasMultipleVPCs;
    public RouterInterfaceResponse removeAnInterfaceToNeutronRouter (String projectid, String portId, String subnetId, String routerId) throws ResourceNotFoundException, ResourcePersistenceException, RouterOrSubnetAndPortNotExistOrNotVisible, AttachedPortsNotMatchPortId, RouterTableNotExist, RouterInterfaceAreUsedByRoutes, SubnetNotBindUniquePortId, DatabasePersistenceException;
    public RoutesToNeutronWebResponse addRoutesToNeutronRouter (String routerid, NewRoutesWebRequest requestRouter) throws ResourceNotFoundException, ResourcePersistenceException, RouterOrSubnetAndPortNotExistOrNotVisible, DatabasePersistenceException, DestinationOrNexthopCanNotBeNull, DestinationSame;
    public RoutesToNeutronWebResponse removeRoutesFromNeutronRouter(String routerid, NewRoutesWebRequest requestRouter) throws RouterOrSubnetAndPortNotExistOrNotVisible, ResourceNotFoundException, ResourcePersistenceException, DestinationOrNexthopCanNotBeNull, DatabasePersistenceException;
    public ConnectedSubnetsWebResponse getConnectedSubnets (String projectId, String vpcId, String subnetId) throws ResourceNotFoundException, ResourcePersistenceException, SubnetNotBindUniquePortId;
    public UpdateRoutingRuleResponse updateRoutingRule (String owner, NewRoutesWebRequest newRouteEntry, boolean isNeutronOrVPCLevelRoutingRule, boolean isAddOperation) throws DestinationOrNexthopCanNotBeNull, CacheException, CanNotFindRouteTableByOwner, QueryParamTypeNotSupportException, RouteTableNotUnique, DestinationInvalid, DatabasePersistenceException;
    public InternalRouterInfo constructInternalRouterInfo (String routerid, List<InternalSubnetRoutingTable> internalSubnetRoutingTableList);
    public List<InternalSubnetRoutingTable> constructInternalSubnetRoutingTables (Router router) throws Exception;
    public List<RouteTable> getRouteTablesBySubnetIds (List<String> subnetIds, String projectid) throws Exception;
}
