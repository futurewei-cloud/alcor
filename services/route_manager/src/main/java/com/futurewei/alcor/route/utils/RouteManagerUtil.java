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
package com.futurewei.alcor.route.utils;

import com.futurewei.alcor.common.enumClass.NetworkStatusEnum;
import com.futurewei.alcor.common.enumClass.NetworkTypeEnum;
import com.futurewei.alcor.common.enumClass.RouteTableType;
import com.futurewei.alcor.common.utils.DateUtil;
import com.futurewei.alcor.web.entity.route.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.text.SimpleDateFormat;
import java.util.*;

public class RouteManagerUtil {

    public static String[] getNullPropertyNames (Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] res = new String[emptyNames.size()];
        return emptyNames.toArray(res);
    }

    /**
     * Overwrite BeanUtils.copyProperties method, and properties with null values are now ignored during the copy process
     * @param src
     * @param target
     */
    public static void copyPropertiesIgnoreNull (Object src, Object target){
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }

    public static boolean checkNeutronRouterWebResourceIsValid(NeutronRouterWebJson resource) {
        if (resource == null) {
            return false;
        }
        NeutronRouterWebRequestObject neutronRouter = resource.getRouter();

        // status
        String status = neutronRouter.getStatus();
        if (!(status == null || NetworkStatusEnum.ACTIVE.getNetworkStatus().equals(status)
                || NetworkStatusEnum.ACTIVE.getNetworkStatus().equals(status)
                || NetworkStatusEnum.DOWN.getNetworkStatus().equals(status)
                || NetworkStatusEnum.BUILD.getNetworkStatus().equals(status)
                || NetworkStatusEnum.ERROR.getNetworkStatus().equals(status))) {
            return false;
        }

        return true;
    }

    public static NeutronRouterWebRequestObject configureNeutronRouterParameters (NeutronRouterWebRequestObject response) {
        if (response == null) {
            return response;
        }

        // availability_zones
        List<String> availabilityZones = response.getAvailabilityZones();
        if (availabilityZones == null) {
            availabilityZones = new ArrayList<String>(){{add("Nova");}};
            response.setAvailabilityZones(availabilityZones);
        }

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

        // conntrack_helpers
        List<String> conntrackHelper = response.getConntrackHelpers();
        if (conntrackHelper == null) {
            response.setConntrackHelpers(new ArrayList<String>());
        }

        return response;
    }

    public static boolean checkVpcDefaultRouteTableWebJsonResourceIsValid(RouteTableWebJson resource) {
        if (resource == null) {
            return false;
        }

        RouteTable routetable = resource.getRoutetable();
        if (routetable == null) {
            return false;
        }

        // routeTableType
        String routeTableTypeStr = resource.getRoutetable().getRouteTableType();
        if (routeTableTypeStr != null && !routeTableTypeStr.equals("vpc")) {
            return false;
        }


        return true;
    }

    public static boolean checkSubnetRouteTableWebJsonResourceIsValid(RouteTableWebJson resource) {
        if (resource == null) {
            return false;
        }

        RouteTable routetable = resource.getRoutetable();
        if (routetable == null) {
            return false;
        }

        // routeTableType
        String routeTableTypeStr = resource.getRoutetable().getRouteTableType();
        if (routeTableTypeStr != null && !routeTableTypeStr.equals("public_subnet") && !routeTableTypeStr.equals("private_subnet")) {
            return false;
        }


        return true;
    }

    public static boolean checkCreateNeutronSubnetRouteTableWebJsonResourceIsValid(RouteTableWebJson resource) {
        if (resource == null) {
            return false;
        }

        RouteTable routetable = resource.getRoutetable();
        if (routetable == null) {
            return false;
        }

        return true;
    }


}
