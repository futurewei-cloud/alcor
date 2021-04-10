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

package com.futurewei.alcor.portmanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.entity.SubnetRoute;
import com.futurewei.alcor.portmanager.exception.GetSubnetEntityException;
import com.futurewei.alcor.portmanager.processor.PortContext;
import com.futurewei.alcor.web.entity.route.RoutesWebJson;
import com.futurewei.alcor.web.restclient.RouteManagerRestClient;

import java.util.ArrayList;
import java.util.List;

public class FetchSubnetRouteRequest extends AbstractRequest {
    private RouteManagerRestClient routeManagerRestClient;
    private List<String> subnetIds;
    private List<SubnetRoute> subnetRoutes;

    public FetchSubnetRouteRequest(PortContext context, List<String> subnetIds) {
        super(context);
        this.subnetIds = subnetIds;
        this.subnetRoutes = new ArrayList<>();
        this.routeManagerRestClient = SpringContextUtil.getBean(RouteManagerRestClient.class);
    }

    public List<SubnetRoute> getSubnetRoutes() {
        return subnetRoutes;
    }

    @Override
    public void send() throws Exception {
        if (subnetIds != null) {
            for (String subnetId: subnetIds) {
                RoutesWebJson routesWebJson = routeManagerRestClient
                        .getSubnetRoute(subnetId);
                if (routesWebJson == null || routesWebJson.getRoutes() == null) {
                    throw new GetSubnetEntityException();
                }

                SubnetRoute subnetRoute = new SubnetRoute(subnetId, routesWebJson.getRoutes());
                subnetRoutes.add(subnetRoute);
            }
        }
    }

    @Override
    public void rollback() {

    }
}
