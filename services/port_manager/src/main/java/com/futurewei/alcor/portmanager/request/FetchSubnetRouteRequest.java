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
package com.futurewei.alcor.portmanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.entity.SubnetRoute;
import com.futurewei.alcor.portmanager.processor.PortContext;
import com.futurewei.alcor.web.entity.route.RoutesWebJson;
import com.futurewei.alcor.web.restclient.RouteManagerRestClient;

import java.util.List;

public class FetchSubnetRouteRequest extends AbstractRequest {
    private RouteManagerRestClient routeManagerRestClient;
    private List<SubnetRoute> subnetRoutes;

    public FetchSubnetRouteRequest(PortContext context, List<SubnetRoute> subnetRoutes) {
        super(context);
        this.subnetRoutes = subnetRoutes;
        this.routeManagerRestClient = SpringContextUtil.getBean(RouteManagerRestClient.class);
    }

    public List<SubnetRoute> getSubnetRoutes() {
        return subnetRoutes;
    }

    @Override
    public void send() throws Exception {
        if (subnetRoutes != null) {
            for (SubnetRoute subnetRoute: subnetRoutes) {
                RoutesWebJson routesWebJson = routeManagerRestClient
                        .getRouteBySubnetId(subnetRoute.getSubnetId());
                subnetRoute.setRouteEntities(routesWebJson.getRoutes());
            }
        }
    }

    @Override
    public void rollback() {

    }
}
