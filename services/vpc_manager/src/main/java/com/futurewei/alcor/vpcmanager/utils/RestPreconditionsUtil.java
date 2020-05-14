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

import com.futurewei.alcor.common.exception.*;
import com.futurewei.alcor.common.entity.CustomerResource;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.web.entity.NetworkSegmentRangeWebResponseObject;
import com.futurewei.alcor.web.entity.SegmentWebResponseObject;
import com.futurewei.alcor.web.entity.VpcWebResponseObject;
import org.thymeleaf.util.StringUtils;

import java.util.logging.Level;

public class RestPreconditionsUtil {

    private static final Logger logger = LoggerFactory.getLogger();

    public static <T> T verifyResourceFound(T resource) throws ResourceNotFoundException {
        if (resource == null) throw new ResourceNotFoundException();

        //TODO: Check resource exists in the repo

        return resource;
    }

    public static <T> T verifyResourceNotExists(T resource) throws ResourcePreExistenceException {
        if (resource == null) throw new ResourcePreExistenceException();

        //TODO: Check resource does not exist in the repo

        return resource;
    }

    public static void verifyResourceNotNull(CustomerResource resource) throws ResourceNullException {
        if (resource == null || StringUtils.isEmpty(resource.getId())) {
            throw new ResourceNullException("Empty resource id");
        }
    }

    public static void verifyParameterNotNullorEmpty(String resourceId) throws ParameterNullOrEmptyException {
        if (StringUtils.isEmpty(resourceId)) {
            throw new ParameterNullOrEmptyException("Empty parameter");
        }
    }

    public static void verifyParameterEqual(String expectedResourceId, String resourceId) throws ParameterUnexpectedValueException {
        if (StringUtils.isEmpty(resourceId) || !resourceId.equalsIgnoreCase(expectedResourceId)) {
            throw new ParameterUnexpectedValueException("Expeceted value: " + expectedResourceId + " | actual: " + resourceId);
        }
    }

    public static void populateResourceProjectId(CustomerResource resource, String projectId) {
        String resourceProjectId = resource.getProjectId();
        if (StringUtils.isEmpty(resourceProjectId)) {
            resource.setProjectId(projectId);
        } else if (!resourceProjectId.equalsIgnoreCase(projectId)) {
            logger.log(Level.INFO, "Resource id not matched " + resourceProjectId + " : " + projectId);
            resource.setProjectId(projectId);
        }
    }

    public static void populateResourceVpcId(CustomerResource resource, String vpcId) {
        String resourceVpcId = null;
        if (resource instanceof VpcWebResponseObject) {
            resourceVpcId = resource.getId();
        }

        if (StringUtils.isEmpty(resourceVpcId)) {
            resource.setId(vpcId);
        } else if (!resourceVpcId.equalsIgnoreCase(vpcId)) {
            logger.log(Level.INFO, "Resource vpc id not matched " + resourceVpcId + " : " + vpcId);
            resource.setId(vpcId);
        }
    }

    public static void populateResourceSegmentId(CustomerResource resource, String segmentId) {
        String resourceSegmentId = null;
        if (resource instanceof SegmentWebResponseObject) {
            resourceSegmentId = resource.getId();
        }

        if (StringUtils.isEmpty(resourceSegmentId)) {
            resource.setId(segmentId);
        } else if (!resourceSegmentId.equalsIgnoreCase(segmentId)) {
            logger.log(Level.INFO, "Resource segment id not matched " + resourceSegmentId + " : " + segmentId);
            resource.setId(segmentId);
        }
    }

    public static void populateResourceSegmentRangeId(CustomerResource resource, String segmentRangeId) {
        String resourceSegmentRangeId = null;
        if (resource instanceof NetworkSegmentRangeWebResponseObject) {
            resourceSegmentRangeId = resource.getId();
        }

        if (StringUtils.isEmpty(resourceSegmentRangeId)) {
            resource.setId(segmentRangeId);
        } else if (!resourceSegmentRangeId.equalsIgnoreCase(segmentRangeId)) {
            logger.log(Level.INFO, "Resource segment range id not matched " + resourceSegmentRangeId + " : " + segmentRangeId);
            resource.setId(segmentRangeId);
        }
    }
}