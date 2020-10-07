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

import com.futurewei.alcor.common.enumClass.NetworkTypeEnum;
import com.futurewei.alcor.common.utils.DateUtil;
import com.futurewei.alcor.web.entity.vpc.SegmentEntity;
import com.futurewei.alcor.web.entity.vpc.SegmentWebRequestJson;
import com.futurewei.alcor.web.entity.vpc.SegmentWebRequest;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SegmentManagementUtil {

    public static boolean checkSegmentRequestResourceIsValid(SegmentWebRequestJson resource) {

        if (resource == null) {
            return false;
        }
        SegmentWebRequest segment = resource.getSegment();

        // network_type
        String networkType = segment.getNetworkType();
        if (!(networkType == null || NetworkTypeEnum.VXLAN.getNetworkType().equals(networkType)
                || NetworkTypeEnum.VLAN.getNetworkType().equals(networkType)
                || NetworkTypeEnum.GRE.getNetworkType().equals(networkType)
                || segment.equals("flat")) ) {
            return false;
        }

        return true;
    }

    public static SegmentEntity configureSegmentDefaultParameters (SegmentEntity response) {
        if (response == null) {
            return response;
        }

        // create_at and update_at
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);

        String utc = DateUtil.localToUTC(dateString, "yyyy-MM-dd HH:mm:ss");

        response.setCreated_at(utc);
        response.setUpdated_at(utc);

        // revision_number
        Integer revisionNumber = response.getRevisionNumber();
        if (revisionNumber == null || revisionNumber < 1) {
            response.setRevisionNumber(1);
        }

        return response;
    }

}
