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
package com.futurewei.alcor.portmanager.proxy;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.entity.PortBindingRoute;
import com.futurewei.alcor.portmanager.exception.GetRouteEntityException;
import com.futurewei.alcor.portmanager.rollback.Rollback;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.route.RoutesWebJson;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.restclient.RouteManagerRestClient;

import java.util.Stack;
import java.util.concurrent.CompletableFuture;

public class RouteManagerProxy {
    private RouteManagerRestClient routeManagerRestClient;
    private Stack<Rollback> rollbacks;

    public RouteManagerProxy(Stack<Rollback> rollbacks) {
        routeManagerRestClient = SpringContextUtil.getBean(RouteManagerRestClient.class);
        this.rollbacks = rollbacks;
    }

    /**
     * Get Subnet route by subnet id
     * @param arg1 Port id
     * @param arg1 Subnet id
     * @return RouteEntity
     * @throws Exception Rest request exception
     */
    public PortBindingRoute getRouteBySubnetId(Object arg1, Object arg2) throws Exception {
        String portId = (String)arg1;
        String subnetId = (String)arg2;
        RoutesWebJson routesWebJson = routeManagerRestClient.getRouteBySubnetId(subnetId);
        if (routesWebJson == null || routesWebJson.getRoutes() == null) {
            throw new GetRouteEntityException();
        }

        // FIXME : we should bind a port with several route rules
        return new PortBindingRoute(portId, routesWebJson.getRoutes().get(0));
    }

    public PortBindingRoute getRouteBySubnetFuture(Object arg1, Object arg2) throws Exception {
        String portId = (String)arg1;
        CompletableFuture subnetFuture = (CompletableFuture)arg2;
        SubnetEntity subnetEntity = (SubnetEntity)subnetFuture.join();
        RoutesWebJson routesWebJson = routeManagerRestClient.getRouteBySubnetId(subnetEntity.getId());
        if (routesWebJson == null || routesWebJson.getRoutes() == null) {
            throw new GetRouteEntityException();
        }

        // FIXME : we should bind a port with several route rules
        return new PortBindingRoute(portId, routesWebJson.getRoutes().get(0));
    }
}
