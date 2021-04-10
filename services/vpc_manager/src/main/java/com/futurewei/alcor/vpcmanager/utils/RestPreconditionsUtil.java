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

import com.futurewei.alcor.common.exception.*;
import com.futurewei.alcor.common.entity.CustomerResource;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.web.entity.vpc.NetworkSegmentRangeEntity;
import com.futurewei.alcor.web.entity.vpc.SegmentEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
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
        if (resource instanceof VpcEntity) {
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
        if (resource instanceof SegmentEntity) {
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
        if (resource instanceof NetworkSegmentRangeEntity) {
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