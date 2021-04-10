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

package com.futurewei.alcor.route.utils;

import com.futurewei.alcor.common.entity.CustomerResource;
import com.futurewei.alcor.common.exception.*;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import org.thymeleaf.util.StringUtils;

public class RestPreconditionsUtil {
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
            System.out.println("Resource id not matched " + resourceProjectId + " : " + projectId);
            resource.setProjectId(projectId);
        }
    }

    public static void populateResourceVpcId(CustomerResource resource, String vpcId) {
        String resourceVpcId = null;
        if (resource instanceof RouteEntity) {
            resourceVpcId = resource.getId();
        }

        if (StringUtils.isEmpty(resourceVpcId)) {
            resource.setId(vpcId);
        } else if (!resourceVpcId.equalsIgnoreCase(vpcId)) {
            System.out.println("Resource vpc id not matched " + resourceVpcId + " : " + vpcId);
            resource.setId(vpcId);
        }
    }
}