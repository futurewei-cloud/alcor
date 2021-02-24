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
