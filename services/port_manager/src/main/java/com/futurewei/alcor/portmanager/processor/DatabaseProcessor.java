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
package com.futurewei.alcor.portmanager.processor;

import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.*;

@AfterProcessor(DataPlaneProcessor.class)
public class DatabaseProcessor extends AbstractProcessor {
    private List<NeighborInfo> buildNeighborInfos(InternalPortEntity internalPortEntity) {
        List<NeighborInfo> neighborInfos = new ArrayList<>();
        String bindingHostIp = internalPortEntity.getBindingHostIP();
        if (bindingHostIp == null) {
            return neighborInfos;
        }

        for (PortEntity.FixedIp fixedIp : internalPortEntity.getFixedIps()) {
            NeighborInfo neighborInfo = new NeighborInfo(bindingHostIp,
                    internalPortEntity.getBindingHostId(),
                    internalPortEntity.getId(),
                    internalPortEntity.getMacAddress(),
                    fixedIp.getIpAddress(),
                    internalPortEntity.getVpcId(),
                    fixedIp.getSubnetId());
            neighborInfos.add(neighborInfo);
        }

        return neighborInfos;
    }

    @Override
    void createProcess(PortContext context) throws Exception {
        Map<String, List<NeighborInfo>> portNeighbors = new HashMap<>();

        NetworkConfig networkConfig = context.getNetworkConfig();
        List<InternalPortEntity> internalPortEntities = networkConfig.getPortEntities();
        for (InternalPortEntity internalPortEntity : internalPortEntities) {
            List<NeighborInfo> neighborInfoList = buildNeighborInfos(internalPortEntity);
            if (neighborInfoList.size() == 0) {
                continue;
            }
            if (!portNeighbors.containsKey(internalPortEntity.getVpcId())) {
                portNeighbors.put(internalPortEntity.getVpcId(), new ArrayList<>());
            }

            portNeighbors.get(internalPortEntity.getVpcId()).addAll(neighborInfoList);
        }

        /**
         * TODO:
         CreateNetworkConfig may fail, in that case we need to rollback the database
         operation, or wait for CreateNetworkConfig to succeed before writing to the database
         */
        List<PortEntity> portEntities = context.getPortEntities();
        context.getPortRepository().createPortBulk(portEntities, portNeighbors);
    }

    @Override
    void updateProcess(PortContext context) throws Exception {
        List<InternalPortEntity> internalPortEntities = context.getNetworkConfig().getPortEntities();
        List<NeighborInfo> neighborInfos = null;
        if (internalPortEntities.size() > 0) {
            neighborInfos = buildNeighborInfos(internalPortEntities.get(0));
        }

        PortEntity oldPortEntity = context.getNewPortEntity();
        PortEntity newPortEntity = context.getNewPortEntity();

        context.getPortRepository().updatePort(oldPortEntity, newPortEntity, neighborInfos);
    }

    @Override
    void deleteProcess(PortContext context) throws Exception {
        //TODO: support batch deletion
        for (PortEntity portEntity : context.getPortEntities()) {
            context.getPortRepository().deletePort(portEntity);
        }
    }
}
