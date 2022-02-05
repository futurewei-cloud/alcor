/*
 *
 * MIT License
 * Copyright(c) 2020 Futurewei Cloud
 *
 *     Permission is hereby granted,
 *     free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
 *     including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
 *     to whom the Software is furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *     WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * /
 */

package com.futurewei.alcor.dataplane.service.impl;

import com.futurewei.alcor.dataplane.cache.SubnetPortsCacheV2;
import com.futurewei.alcor.dataplane.cache.VpcPathCache;
import com.futurewei.alcor.dataplane.cache.VpcSubnetsCache;
import com.futurewei.alcor.dataplane.exception.InvalidPathModeException;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.subnet.InternalSubnetPorts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Configuration
public class PathManagerService {
    @Autowired
    private VpcSubnetsCache vpcSubnetsCache;

    @Autowired
    private SubnetPortsCacheV2 subnetPortsCache;

    @Autowired
    private VpcPathCache vpcPathCache;

    private static boolean USE_GRPC = true;

    @Value("${path.mode}")
    private String PATH_MODE;

    @Value("${path.UPPER_VPC_SIZE}")
    private int UPPER_VPC_SIZE;
    @Value("${path.LOWER_VPC_SIZE}")
    private int LOWER_VPC_SIZE;

    public boolean isFastPath() {
        return USE_GRPC;
    }

    public boolean isFastPath(PortEntity portEntity) throws Exception{
        switch (PATH_MODE) {
            case "GRPC":
                return USE_GRPC;
            case "MQ":
                return !USE_GRPC;
            case "MIXED":
                return choosePath(portEntity);
            default:
                throw new InvalidPathModeException();
        }
    }

    private boolean choosePath(PortEntity portEntity) throws Exception{
        if (portEntity == null) {
            return USE_GRPC;
        }

        String vpcId = portEntity.getVpcId();

        // Get VPC size - number of ports
        AtomicInteger atomicNumberOfPorts = new AtomicInteger();
        List<String> subnetIds;
        subnetIds = vpcSubnetsCache.getVpcSubnets(vpcId).getSubnetIds();
        subnetIds.stream().forEach(subnetId -> {
            try {
                InternalSubnetPorts internalSubnetPorts = subnetPortsCache.getSubnetPorts(subnetId);
                atomicNumberOfPorts.addAndGet(internalSubnetPorts.getPorts().size());
            } catch (Exception e) {

            }
        });

        int numberOfPorts = atomicNumberOfPorts.get();

        // Get the current path for this path
        boolean currentPath;
        try {
            currentPath = vpcPathCache.getCurrentPathByVpcId(vpcId);
        } catch (NullPointerException e) {
            if (numberOfPorts > UPPER_VPC_SIZE) {
                vpcPathCache.setPath(vpcId, USE_GRPC);
                return USE_GRPC;
            } else {
                vpcPathCache.setPath(vpcId, !USE_GRPC);
                return !USE_GRPC;
            }

        } catch (Exception e) {
            throw e;
        }

        // Path switch logic
        boolean chosenPath;

        if (currentPath == USE_GRPC) {
            if (numberOfPorts > UPPER_VPC_SIZE) {
                chosenPath = !USE_GRPC;
            } else {
                chosenPath = USE_GRPC;
            }
        } else {
            if (numberOfPorts < LOWER_VPC_SIZE) {
                chosenPath = USE_GRPC;
            } else {
                chosenPath = !USE_GRPC;
            }
        }

        if (! currentPath == chosenPath) {
            vpcPathCache.setPath(vpcId, chosenPath);
        }

        return chosenPath;
    }
}
