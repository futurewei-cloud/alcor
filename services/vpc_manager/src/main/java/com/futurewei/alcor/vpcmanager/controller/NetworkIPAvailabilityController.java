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
package com.futurewei.alcor.vpcmanager.controller;

import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.vpcmanager.service.NetworkIPAvailabilityService;
import com.futurewei.alcor.vpcmanager.utils.RestPreconditionsUtil;
import com.futurewei.alcor.web.entity.vpc.NetworkIPAvailabilitiesWebJson;
import com.futurewei.alcor.web.entity.vpc.NetworkIPAvailabilityEntity;
import com.futurewei.alcor.web.entity.vpc.NetworkIPAvailabilityWebJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class NetworkIPAvailabilityController {

    @Autowired
    NetworkIPAvailabilityService networkIPAvailabilityService;

    /**
     * Shows network IP availability details for a network.
     * @param vpcid
     * @return Network IP Availability Entity Json
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = {"/network-ip-availabilities/{vpcid}"})
    public NetworkIPAvailabilityWebJson getNetworkIPAvailabilityByVpcId(@PathVariable String vpcid) throws Exception {

        NetworkIPAvailabilityEntity networkIPAvailabilityEntity = null;

        try {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(vpcid);

            networkIPAvailabilityEntity = this.networkIPAvailabilityService.getNetworkIPAvailability(vpcid);
        } catch (ParameterNullOrEmptyException e) {
            throw new Exception(e);
        }

        if (networkIPAvailabilityEntity == null) {
            return new NetworkIPAvailabilityWebJson();
        }

        return new NetworkIPAvailabilityWebJson(networkIPAvailabilityEntity);
    }

    /**
     * Lists network IP availability of all networks.
     * @return Network IP Availabilities Json
     * @throws Exception
     */
    @RequestMapping(
            method = GET,
            value = {"/network-ip-availabilities"})
    public NetworkIPAvailabilitiesWebJson getNetworkIPAvailabilities(@RequestParam(value = "network_id", required = false) String vpcId,
                                                                     @RequestParam(value = "network_name", required = false) String vpcName,
                                                                     @RequestParam(value = "tenant_id", required = false) String tenantId,
                                                                     @RequestParam(value = "project_id", required = false) String projectId) throws Exception {
        List<NetworkIPAvailabilityEntity> networkIPAvailabilities = null;

        try {

            networkIPAvailabilities = this.networkIPAvailabilityService.getNetworkIPAvailabilities(vpcId, vpcName, tenantId, projectId);

        } catch (Exception e) {
            throw new Exception(e);
        }

        if (networkIPAvailabilities == null) {
            return new NetworkIPAvailabilitiesWebJson();
        }

        return new NetworkIPAvailabilitiesWebJson(networkIPAvailabilities);
    }

}
