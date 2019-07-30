package com.futurewei.alioth.controller.web.util;

import com.futurewei.alioth.controller.exception.ResourceNullException;
import com.futurewei.alioth.controller.exception.ResourceNullOrEmptyException;
import com.futurewei.alioth.controller.model.SubnetState;
import com.futurewei.alioth.controller.model.VpcState;
import com.futurewei.alioth.controller.exception.ResourceNotFoundException;
import org.thymeleaf.util.StringUtils;

public class RestPreconditions {
    public static <T> T checkFound(T resource) throws ResourceNotFoundException {
        if (resource == null) throw new ResourceNotFoundException();
        return resource;
    }

    public static void checkNotNull(VpcState resource) throws ResourceNullException {
        System.out.println("Let's inspect resource:");
        System.out.println("Id: " + resource.getId());
        System.out.println("ProjectId: " + resource.getProjectId());
        System.out.println("Name: " + resource.getName());
        System.out.println("Cidr: " + resource.getCidr());

        if (resource.getId() == null || resource.getId() == "") {
            throw new ResourceNullException("Empty resource id");
        }
    }

    public static void checkNotNull(SubnetState resource) throws ResourceNullException {
        if (resource == null || resource.getId() == null || resource.getId() == "") {
            throw new ResourceNullException("Empty resource id");
        }
    }

    public static void checkNotNullorEmpty(String parameter) throws ResourceNullOrEmptyException {
        if(StringUtils.isEmpty(parameter)) {
            throw new ResourceNullOrEmptyException("Empty parameter");
        }
    }
}