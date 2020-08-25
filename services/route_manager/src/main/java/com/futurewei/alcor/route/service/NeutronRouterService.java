/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.route.service;

import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourcePersistenceException;
import com.futurewei.alcor.route.exception.*;
import com.futurewei.alcor.web.entity.route.NeutronRouterWebRequestObject;
import com.futurewei.alcor.web.entity.route.RouterInterfaceResponse;
import com.futurewei.alcor.web.entity.route.RoutesToNeutronRouterRequestObject;
import com.futurewei.alcor.web.entity.route.RoutesToNeutronWebResponse;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.entity.subnet.SubnetsWebJson;

public interface NeutronRouterService {

    public NeutronRouterWebRequestObject getNeutronRouter (String routerId) throws ResourceNotFoundException, ResourcePersistenceException, CanNotFindRouter;
    public void saveRouterAndRouterExtraAttribute (NeutronRouterWebRequestObject neutronRouter) throws NeutronRouterIsNull, DatabasePersistenceException;
    public RouterInterfaceResponse addAnInterfaceToNeutronRouter (String portId, String subnetId, String routerId) throws SpecifyBothSubnetIDAndPortID, ResourceNotFoundException, ResourcePersistenceException, CanNotFindRouter, DatabasePersistenceException, PortIDIsAlreadyExist, PortIsAlreadyInUse, SubnetNotBindUniquePortId;
    public RouterInterfaceResponse removeAnInterfaceToNeutronRouter (String portId, String subnetId, String routerId) throws ResourceNotFoundException, ResourcePersistenceException, RouterOrSubnetAndPortNotExistOrNotVisible, AttachedPortsNotMatchPortId, RouterTableNotExist, RouterInterfaceAreUsedByRoutes, SubnetNotBindUniquePortId;
    public RoutesToNeutronWebResponse addRoutesToNeutronRouter (String routerid, RoutesToNeutronRouterRequestObject requestRouter) throws ResourceNotFoundException, ResourcePersistenceException, RouterOrSubnetAndPortNotExistOrNotVisible, DatabasePersistenceException, DestinationOrNexthopCanNotBeNull;
    public RoutesToNeutronWebResponse removeRoutesToNeutronRouter (String routerid, RoutesToNeutronRouterRequestObject requestRouter) throws RouterOrSubnetAndPortNotExistOrNotVisible, ResourceNotFoundException, ResourcePersistenceException, DestinationOrNexthopCanNotBeNull, DatabasePersistenceException;
    public SubnetWebJson getSubnet (String subnetId);
    public SubnetsWebJson getSubnetsByPortId (String portId);
    public void updateSubnet (String projectid, String subnetId, SubnetEntity subnet);
}
