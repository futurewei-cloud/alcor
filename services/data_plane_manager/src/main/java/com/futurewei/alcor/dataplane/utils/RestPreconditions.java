package com.futurewei.alcor.dataplane.utils;

import com.futurewei.alcor.common.entity.CustomerResource;
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.exception.ParameterUnexpectedValueException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.exception.ResourceNullException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.dataplane.config.Config;
import org.thymeleaf.util.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Level;

import static com.futurewei.alcor.schema.Subnet.SubnetState;
import static com.futurewei.alcor.schema.Vpc.VpcState;
//TODO leave it for now
public class RestPreconditions {
    public static <T> T verifyResourceFound(T resource) throws ResourceNotFoundException {
        if (resource == null) throw new ResourceNotFoundException();

        //TODO: Check resource exists in the repo

        return resource;
    }

    public static void verifyResourceNotNull(Object resource) throws ResourceNullException {
        if (resource == null || StringUtils.isEmpty(((CustomerResource)resource).getId())) {
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

    public static void populateResourceProjectId(Object resource, String projectId) {
        String resourceProjectId = ((CustomerResource)resource).getProjectId();
        if (StringUtils.isEmpty(resourceProjectId)) {
            ((CustomerResource)resource).setProjectId(projectId);
        } else if (!resourceProjectId.equalsIgnoreCase(projectId)) {
            System.out.println("Resource id not matched " + resourceProjectId + " : " + projectId);
            ((CustomerResource)resource).setProjectId(projectId);
        }
    }

    public static void populateResourceVpcId(Object resource, String vpcId) {
        String resourceVpcId = null;
        if (resource instanceof VpcState) {
            resourceVpcId = ((VpcState) resource).getConfiguration().getId();
        } else if (resource instanceof SubnetState) {
            resourceVpcId = ((SubnetState) resource).getConfiguration().getVpcId();
        }

        if (StringUtils.isEmpty(resourceVpcId)) {
            ((CustomerResource)resource).setId(vpcId);
        } else if (!resourceVpcId.equalsIgnoreCase(vpcId)) {
            System.out.println("Resource vpc id not matched " + resourceVpcId + " : " + vpcId);
            ((CustomerResource)resource).setId(vpcId);
        }
    }

    public static void recordRequestTimeStamp(String resourceId, long T0, long T1, long[] timeArray) {
        BufferedWriter timeStampWriter = Config.TIME_STAMP_WRITER;
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
            Config.TOTAL_TIME += elapseTimeInMs;
            Config.TOTAL_REQUEST++;
            if (elapseTimeInMs < Config.MIN_TIME) Config.MIN_TIME = elapseTimeInMs;
            if (elapseTimeInMs > Config.MAX_TIME) Config.MAX_TIME = elapseTimeInMs;

            if (Config.TOTAL_REQUEST == Config.epHosts.size() * Config.EP_PER_HOST) {
                timeStampWriter.newLine();
                timeStampWriter.write("," + Config.TOTAL_TIME / Config.TOTAL_REQUEST + "," + Config.MIN_TIME + "," + Config.MAX_TIME);
                timeStampWriter.newLine();
                timeStampWriter.write("Average time of " + Config.TOTAL_REQUEST + " requests :" +
                        Config.TOTAL_TIME / Config.TOTAL_REQUEST + " ms");
                timeStampWriter.newLine();
                timeStampWriter.write("Time span: " + (System.nanoTime() - Config.APP_START_TS) / 1000000 + " ms");
                timeStampWriter.flush();

                if (timeStampWriter != null)
                    timeStampWriter.close();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } finally {
            try {
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error in closing the BufferedWriter", ex);
            }
        }
    }
}
