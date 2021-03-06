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
import com.futurewei.alcor.web.entity.vpc.NetworkSegmentRangeEntity;
import com.futurewei.alcor.web.entity.vpc.NetworkSegmentRangeWebRequestJson;
import com.futurewei.alcor.web.entity.vpc.NetworkSegmentRangeWebRequest;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SegmentRangeManagementUtil {

    public static boolean checkSegmentRangeRequestResourceIsValid(NetworkSegmentRangeWebRequestJson resource) {

        if (resource == null) {
            return false;
        }
        NetworkSegmentRangeWebRequest segmentRange = resource.getNetwork_segment_range();

        // network_type
        String networkType = segmentRange.getNetworkType();
        if (!(networkType == null || NetworkTypeEnum.VXLAN.getNetworkType().equals(networkType)
                || NetworkTypeEnum.VLAN.getNetworkType().equals(networkType)
                || NetworkTypeEnum.GRE.getNetworkType().equals(networkType)
                || segmentRange.equals("flat")) ) {
            return false;
        }

        // minimum
        Integer minimum = segmentRange.getMinimum();
        if (minimum != null && minimum < 0) {
            return false;
        }

        // maximum
        Integer maximum = segmentRange.getMaximum();
        if (maximum != null && minimum != null && maximum < minimum) {
            return false;
        }

        return true;
    }

    public static NetworkSegmentRangeEntity configureSegmentRangeDefaultParameters (NetworkSegmentRangeEntity response) {
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
