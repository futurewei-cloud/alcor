package com.futurewei.alioth.controller.web.util;

import com.futurewei.alioth.controller.exception.*;
import com.futurewei.alioth.controller.model.CustomerResource;
import com.futurewei.alioth.controller.exception.ResourceNotFoundException;
import com.futurewei.alioth.controller.model.SubnetState;
import com.futurewei.alioth.controller.model.VpcState;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;

import static com.futurewei.alioth.controller.app.demo.DemoConfig.*;

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

    public static void verifyParameterEqual(String expectedResourceId, String resourceId) throws ParameterUnexpectedValueException {
        if(StringUtils.isEmpty(resourceId) || !resourceId.equalsIgnoreCase(expectedResourceId)){
            throw new ParameterUnexpectedValueException("Expeceted value: " + expectedResourceId + " | actual: " + resourceId);
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

    public static void recordRequestTimeStamp(String resourceId, long T0, long T1, long[] timeArray){
        try {
            TIME_STAMP_WRITER.newLine();

            long timeElapsedInMsForDataPersistence = (T1 - T0) / 1000000;
            long timeElapsedInMsForFirstMessaging = (timeArray[0] - T1) / 1000000;
            TIME_STAMP_WRITER.write(resourceId + "," + timeElapsedInMsForDataPersistence + "," +
                    timeElapsedInMsForFirstMessaging + ",");
            for (int i = 0; i < timeArray.length - 1 ; i++) {
                long timestampInMs = (timeArray[i+1] - timeArray[i]) / 1000000;
                TIME_STAMP_WRITER.write(timestampInMs + ",");
            }

            TOTAL_TIME += (timeArray[timeArray.length-1] - T0) / 1000000;
            TOTAL_REQUEST ++;

            if(TOTAL_REQUEST % 1000 == 0){
                TIME_STAMP_WRITER.newLine();
                TIME_STAMP_WRITER.write("Average time of " + TOTAL_REQUEST + " requests :" +
                        TOTAL_TIME/TOTAL_REQUEST + " ms");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}