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

package com.futurewei.alcor.dataplane.service;

import com.futurewei.alcor.dataplane.cache.VpcGatewayInfoCache;
import com.futurewei.alcor.web.entity.gateway.GatewayInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ComponentScan("com.futurewei.alcor.web.restclient")
public class GatewayCacheService {

    @Autowired
    private VpcGatewayInfoCache vpcGatewayInfoCache;

    public GatewayCacheService() { }

    public String createGatewayInfo(GatewayInfo gatewayInfo) throws Exception {
        GatewayInfo gatewayInfoCache = vpcGatewayInfoCache.findItem(gatewayInfo.getResourceId());
        String resourceId = null;
        if (gatewayInfoCache != null) {
            log.error("GatewayInfo for {} already exists!", gatewayInfo.getResourceId());
        } else {
            vpcGatewayInfoCache.addItem(gatewayInfo);
            if (vpcGatewayInfoCache.findItem(gatewayInfo.getResourceId()) != null) {
                resourceId = gatewayInfo.getResourceId();
            }
        }
        return resourceId;
    }

    public String updateGatewayInfo(String resource_id, GatewayInfo gatewayInfo) throws Exception {
        GatewayInfo gatewayInfoCache = vpcGatewayInfoCache.findItem(resource_id);
        String resourceId = null;
        if (gatewayInfoCache == null) {
            log.error("GatewayInfo for {} is not exists!", gatewayInfo.getResourceId());
        } else {
            vpcGatewayInfoCache.updateVpcGatewayInfo(gatewayInfo);
            if (vpcGatewayInfoCache.findItem(gatewayInfo.getResourceId()) != null) {
                resourceId = gatewayInfo.getResourceId();
            }
        }
        return resourceId;
    }

    public void deleteGatewayInfo(String resource_id) throws Exception {
        GatewayInfo gatewayInfoCache = vpcGatewayInfoCache.findItem(resource_id);
        if (gatewayInfoCache == null) {
            log.error("GatewayInfo for {} is not exists!", resource_id);
        } else {
            vpcGatewayInfoCache.deleteItem(resource_id);
        }
    }

}
