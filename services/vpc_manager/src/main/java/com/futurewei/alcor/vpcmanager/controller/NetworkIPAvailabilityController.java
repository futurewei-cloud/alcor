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
package com.futurewei.alcor.vpcmanager.controller;

import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.utils.ControllerUtil;
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

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class NetworkIPAvailabilityController {

    @Autowired
    NetworkIPAvailabilityService networkIPAvailabilityService;

    @Autowired
    private HttpServletRequest request;

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

        Map<String, Object[]> queryParams =
                ControllerUtil.transformUrlPathParams(request.getParameterMap(), NetworkIPAvailabilityEntity.class);
        ControllerUtil.handleUserRoles(request.getHeader(ControllerUtil.TOKEN_INFO_HEADER), queryParams);
        try {

            networkIPAvailabilities = this.networkIPAvailabilityService.getNetworkIPAvailabilities(queryParams);

        } catch (Exception e) {
            throw new Exception(e);
        }

        if (networkIPAvailabilities == null) {
            return new NetworkIPAvailabilitiesWebJson();
        }

        return new NetworkIPAvailabilitiesWebJson(networkIPAvailabilities);
    }

}
