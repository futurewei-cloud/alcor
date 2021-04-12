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
