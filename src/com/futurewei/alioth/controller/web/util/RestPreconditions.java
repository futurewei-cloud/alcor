package com.futurewei.alioth.controller.web.util;

import com.futurewei.alioth.controller.model.VpcState;
import com.futurewei.alioth.controller.exception.ResourceNotFoundException;

public class RestPreconditions {
    public static <T> T checkFound(T resource) throws ResourceNotFoundException {
        if (resource == null) throw new ResourceNotFoundException();
        return resource;
    }

    public static void checkNotNull(VpcState resource) throws Exception {
        System.out.println("Let's inspect resource:");
        System.out.println("Id: " + resource.getId());
        System.out.println("ProjectId: " + resource.getProjectId());
        System.out.println("Name: " + resource.getName());
        System.out.println("Cidr: " + resource.getCidr());

        if(resource.getId() == null || resource.getId() == ""){
            throw new Exception("Empty resource id");
        }
    }
}