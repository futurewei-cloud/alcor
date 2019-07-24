package com.futurewei.alioth.controller.web.util;

import com.futurewei.alioth.controller.model.VpcState;
import com.futurewei.alioth.controller.exception.ResourceNotFoundException;

public class RestPreconditions {
    public static <T> T checkFound(T resource) throws ResourceNotFoundException {
        if (resource == null) throw new ResourceNotFoundException();
        return resource;
    }

    public static void checkNotNull(VpcState resource) {
    }
}