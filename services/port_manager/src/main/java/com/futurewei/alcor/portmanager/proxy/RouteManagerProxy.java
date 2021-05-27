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
package com.futurewei.alcor.portmanager.proxy;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.entity.PortBindingRoute;
import com.futurewei.alcor.portmanager.exception.GetRouteEntityException;
import com.futurewei.alcor.portmanager.rollback.Rollback;
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
        RoutesWebJson routesWebJson = routeManagerRestClient.getSubnetRoute(subnetId);
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
        RoutesWebJson routesWebJson = routeManagerRestClient.getSubnetRoute(subnetEntity.getId());
        if (routesWebJson == null || routesWebJson.getRoutes() == null) {
            throw new GetRouteEntityException();
        }

        // FIXME : we should bind a port with several route rules
        return new PortBindingRoute(portId, routesWebJson.getRoutes().get(0));
    }
}
