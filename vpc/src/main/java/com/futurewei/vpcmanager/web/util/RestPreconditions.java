package com.futurewei.vpcmanager.web.util;

import com.futurewei.vpcmanager.app.onebox.OneBoxConfig;
import com.futurewei.vpcmanager.exception.*;
import com.futurewei.vpcmanager.logging.Logger;
import com.futurewei.vpcmanager.logging.LoggerFactory;
import com.futurewei.vpcmanager.model.CustomerResource;
import com.futurewei.vpcmanager.model.VpcState;
import org.thymeleaf.util.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Level;

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
        if (resource instanceof VpcState) {
            resourceVpcId = resource.getId();
        }

        if (StringUtils.isEmpty(resourceVpcId)) {
            resource.setId(vpcId);
        } else if (!resourceVpcId.equalsIgnoreCase(vpcId)) {
            System.out.println("Resource vpc id not matched " + resourceVpcId + " : " + vpcId);
            resource.setId(vpcId);
        }
    }

    public static void recordRequestTimeStamp(String resourceId, long T0, long T1, long[] timeArray) {
        BufferedWriter timeStampWriter = OneBoxConfig.TIME_STAMP_WRITER;
        Logger logger = LoggerFactory.getLogger();
        try {
            //timeStampWriter = new BufferedWriter(TIME_STAMP_FILE);
            timeStampWriter.newLine();

            long timeElapsedInMsForDataPersistence = (T1 - T0) / 1000000;
            long timeElapsedInMsForFirstMessaging = (timeArray[0] - T1) / 1000000;
            timeStampWriter.write(resourceId + "," + timeElapsedInMsForDataPersistence + "," +
                    timeElapsedInMsForFirstMessaging + ",");
            for (int i = 0; i < timeArray.length - 1; i++) {
                long timestampInMs = (timeArray[i + 1] - timeArray[i]) / 1000000;
                timeStampWriter.write(timestampInMs + ",");
            }
            timeStampWriter.flush();

            long elapseTimeInMs = (timeArray[timeArray.length - 1] - T0) / 1000000;
            OneBoxConfig.TOTAL_TIME += elapseTimeInMs;
            OneBoxConfig.TOTAL_REQUEST++;
            if (elapseTimeInMs < OneBoxConfig.MIN_TIME) OneBoxConfig.MIN_TIME = elapseTimeInMs;
            if (elapseTimeInMs > OneBoxConfig.MAX_TIME) OneBoxConfig.MAX_TIME = elapseTimeInMs;

            if (OneBoxConfig.TOTAL_REQUEST == OneBoxConfig.epHosts.size() * OneBoxConfig.EP_PER_HOST) {
                timeStampWriter.newLine();
                timeStampWriter.write("," + OneBoxConfig.TOTAL_TIME / OneBoxConfig.TOTAL_REQUEST + "," + OneBoxConfig.MIN_TIME + "," + OneBoxConfig.MAX_TIME);
                timeStampWriter.newLine();
                timeStampWriter.write("Average time of " + OneBoxConfig.TOTAL_REQUEST + " requests :" +
                        OneBoxConfig.TOTAL_TIME / OneBoxConfig.TOTAL_REQUEST + " ms");
                timeStampWriter.newLine();
                timeStampWriter.write("Time span: " + (System.nanoTime() - OneBoxConfig.APP_START_TS) / 1000000 + " ms");
                timeStampWriter.flush();

                if (timeStampWriter != null)
                    timeStampWriter.close();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } finally {
            try {
//                if(timeStampWriter != null)
//                    timeStampWriter.close();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error in closing the BufferedWriter", ex);
            }
        }
    }
}