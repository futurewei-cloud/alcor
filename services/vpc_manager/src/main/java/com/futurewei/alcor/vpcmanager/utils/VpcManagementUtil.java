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
package com.futurewei.alcor.vpcmanager.utils;

import com.futurewei.alcor.common.enumClass.NetworkStatusEnum;
import com.futurewei.alcor.common.enumClass.NetworkTypeEnum;
import com.futurewei.alcor.web.entity.SegmentInfoInVpc;
import com.futurewei.alcor.web.entity.VpcWebRequestJson;
import com.futurewei.alcor.web.entity.VpcWebRequestObject;
import com.futurewei.alcor.web.entity.VpcWebResponseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VpcManagementUtil {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static boolean checkVpcCreateResourceIsValid (VpcWebRequestJson resource) {
        if (resource == null) {
            return false;
        }
        VpcWebRequestObject network = resource.getNetwork();

        // mtu
        Integer mtu = network.getMtu();
        if (mtu < 68) {
            return false;
        }

        // network_type
        String networkType = network.getNetworkType();
        if (!(networkType == null || NetworkTypeEnum.VXLAN.getNetworkType().equals(networkType)
        || NetworkTypeEnum.VLAN.getNetworkType().equals(networkType)
        || NetworkTypeEnum.GRE.getNetworkType().equals(networkType)
        || network.equals("flat")) ) {
            return false;
        }

        // status
        String status = network.getStatus();
        if (!(status == null || NetworkStatusEnum.ACTIVE.getNetworkStatus().equals(status)
        || NetworkStatusEnum.ACTIVE.getNetworkStatus().equals(status)
        || NetworkStatusEnum.DOWN.getNetworkStatus().equals(status)
        || NetworkStatusEnum.BUILD.getNetworkStatus().equals(status)
        || NetworkStatusEnum.ERROR.getNetworkStatus().equals(status))) {
            return false;
        }

        return true;
    }

    public static VpcWebResponseObject configureNetworkDefaultParameters (VpcWebResponseObject response) {
        if (response == null) {
            return response;
        }

        // availability_zones
        List<String> availabilityZones = response.getAvailabilityZones();
        if (availabilityZones == null) {
            availabilityZones = new ArrayList<String>(){{add("Nova");}};
            response.setAvailabilityZones(availabilityZones);
        }

        // create_at and update_at
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        response.setCreated_at(dateString);
        response.setUpdated_at(dateString);

        // revision_number
        Integer revisionNumber = response.getRevisionNumber();
        if (revisionNumber == null || revisionNumber < 1) {
            response.setRevisionNumber(1);
        }

        // tenant_id
        String tenantId = response.getTenantId();
        if (tenantId == null) {
            response.setTenantId(response.getProjectId());
        }

        // tags
        List<String> tags = response.getTags();
        if (tags == null) {
            tags = new ArrayList<String>(){{add("tag1,tag2");}};
            response.setTags(tags);
        }

        // status
        String status = response.getStatus();
        if (status == null) {
            response.setStatus(NetworkStatusEnum.ACTIVE.getNetworkStatus());
        }

        return response;
    }

}
