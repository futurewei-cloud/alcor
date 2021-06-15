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
package com.futurewei.alcor.route.utils;

import com.futurewei.alcor.common.enumClass.NetworkStatusEnum;
import com.futurewei.alcor.web.entity.route.NeutronRouterWebJson;
import com.futurewei.alcor.web.entity.route.NeutronRouterWebRequestObject;
import com.futurewei.alcor.web.entity.route.RouteTable;
import com.futurewei.alcor.web.entity.route.RouteTableWebJson;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        if (!(status == null
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
        if (!NetworkStatusEnum.ACTIVE.getNetworkStatus().equals(status)) {
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
        if (routeTableTypeStr != null && !routeTableTypeStr.equals("public_subnet") && !routeTableTypeStr.equals("private_subnet") && !routeTableTypeStr.equals("neutron_subnet")) {
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
