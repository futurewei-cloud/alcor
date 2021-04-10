/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.futurewei.alcor.controller.web.util;

import com.futurewei.alcor.controller.app.onebox.OneBoxConfig;
import com.futurewei.alcor.controller.exception.*;
import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
import com.futurewei.alcor.controller.model.CustomerResource;
import com.futurewei.alcor.controller.model.SubnetState;
import com.futurewei.alcor.controller.model.VpcState;
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
        } else if (resource instanceof SubnetState) {
            resourceVpcId = ((SubnetState) resource).getVpcId();
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