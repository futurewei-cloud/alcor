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

package com.futurewei.alcor.vpcmanager.utils;

import com.futurewei.alcor.common.enumClass.NetworkStatusEnum;
import com.futurewei.alcor.common.enumClass.NetworkTypeEnum;
import com.futurewei.alcor.common.utils.DateUtil;
import com.futurewei.alcor.vpcmanager.config.ConstantsConfig;
import com.futurewei.alcor.web.entity.vpc.*;
import com.futurewei.alcor.web.entity.vpc.VpcWebRequestJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VpcManagementUtil {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static boolean checkVpcRequestResourceIsValid(VpcWebRequestJson resource) {
        if (resource == null) {
            return false;
        }
        VpcWebRequest network = resource.getNetwork();

        // mtu
        Integer mtu = network.getMtu();
        if (mtu != null && mtu < 68) {
            return false;
        }

        // network_type
        String networkType = network.getNetworkType();
        Integer segmentationId = network.getSegmentationId();
        if (!(NetworkTypeEnum.VXLAN.getNetworkType().equals(networkType)
        || NetworkTypeEnum.VLAN.getNetworkType().equals(networkType)
        || NetworkTypeEnum.GRE.getNetworkType().equals(networkType)
        || NetworkTypeEnum.FLAT.getNetworkType().equals(networkType)
        || (networkType == null && segmentationId == null)) ) {
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

    public static VpcEntity configureNetworkDefaultParameters (VpcEntity response) {
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

        String utc = DateUtil.localToUTC(dateString, "yyyy-MM-dd HH:mm:ss");

        response.setCreated_at(utc);
        response.setUpdated_at(utc);

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
            tags = new ArrayList<String>();
            response.setTags(tags);
        }

        // status
        String status = response.getStatus();
        if (status == null) {
            response.setStatus(NetworkStatusEnum.ACTIVE.getNetworkStatus());
        }

        //TODO: mtu
        Integer mtu = response.getMtu();
        if (mtu == null || mtu <= 0) {
            response.setMtu(ConstantsConfig.MTU);
        }

        return response;
    }

}
