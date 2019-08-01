package com.futurewei.alioth.controller.web.util;

import com.futurewei.alioth.controller.exception.*;
import com.futurewei.alioth.controller.model.CustomerResource;
import com.futurewei.alioth.controller.exception.ResourceNotFoundException;
import com.futurewei.alioth.controller.model.SubnetState;
import com.futurewei.alioth.controller.model.VpcState;
import org.thymeleaf.util.StringUtils;

public class RestPreconditions {
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
        if(StringUtils.isEmpty(resourceId)) {
            throw new ParameterNullOrEmptyException("Empty parameter");
        }
    }

    public static void populateResourceProjectId(CustomerResource resource, String projectId) {
        String resourceProjectId = resource.getProjectId();
        if (StringUtils.isEmpty(resourceProjectId)) {
            resource.setProjectId(projectId);
        }else if(!resourceProjectId.equalsIgnoreCase(projectId)){
            System.out.println("Resource id not matched " + resourceProjectId + " : " + projectId);
            resource.setProjectId(projectId);
        }
    }

    public static void populateResourceVpcId(CustomerResource resource, String vpcId) {
        String resourceVpcId = null;
        if(resource instanceof VpcState){
            resourceVpcId = resource.getId();
        }else if(resource instanceof SubnetState){
            resourceVpcId = ((SubnetState) resource).getVpcId();
        }

        if (StringUtils.isEmpty(resourceVpcId)) {
            resource.setId(vpcId);
        }else if(!resourceVpcId.equalsIgnoreCase(vpcId)){
            System.out.println("Resource vpc id not matched " + resourceVpcId + " : " + vpcId);
            resource.setId(vpcId);
        }
    }

}