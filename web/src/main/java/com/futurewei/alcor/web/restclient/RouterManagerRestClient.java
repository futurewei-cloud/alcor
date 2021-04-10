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

package com.futurewei.alcor.web.restclient;

import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.route.ConnectedSubnetsWebResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouterManagerRestClient extends AbstractRestClient {
    @Value("${microservices.router.service.url:#{\"\"}}")
    private String routerManagerUrl;

    public ConnectedSubnetsWebResponse getRouterSubnets(String projectId, String vpcId, String subnetId) throws Exception {
        String url = String.format("/project/%s/vpcs/%s/subnets/%s/connected-subnets", projectId, vpcId, subnetId);
        url = routerManagerUrl + url;
        ConnectedSubnetsWebResponse routerSubnets = restTemplate.getForObject(url, ConnectedSubnetsWebResponse.class);
        if (routerSubnets == null) {
            throw new Exception("Get router connected subnets failed");
        }

        return routerSubnets;
    }
}
