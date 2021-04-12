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
package com.futurewei.alcor.dataplane.service.impl;

import com.futurewei.alcor.dataplane.entity.UnicastGoalState;
import com.futurewei.alcor.dataplane.exception.VpcEntityNotFound;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.Port;
import com.futurewei.alcor.schema.Vpc;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VpcService extends ResourceService {
    private VpcEntity getVpcEntity(NetworkConfiguration networkConfig, String vpcId) throws Exception {
        VpcEntity result = null;
        for (VpcEntity vpcEntity: networkConfig.getVpcs()) {
            if (vpcEntity.getId().equals(vpcId)) {
                result = vpcEntity;
            }
        }

        if (result == null) {
            throw new VpcEntityNotFound();
        }

        return result;
    }

    public void buildVpcStates(NetworkConfiguration networkConfig, UnicastGoalState unicastGoalState) throws Exception {
        List<Port.PortState> portStates = unicastGoalState.getGoalStateBuilder().getPortStatesList();
        if (portStates == null || portStates.size() == 0) {
            return;
        }

        for (Port.PortState portState: portStates) {
            VpcEntity vpcEntity = getVpcEntity(networkConfig, portState.getConfiguration().getVpcId());
            Vpc.VpcConfiguration.Builder vpcConfigBuilder = Vpc.VpcConfiguration.newBuilder();
            vpcConfigBuilder.setRevisionNumber(FORMAT_REVISION_NUMBER);
            vpcConfigBuilder.setId(vpcEntity.getId());
            vpcConfigBuilder.setProjectId(vpcEntity.getProjectId());

            if (vpcEntity.getName() != null) {
                vpcConfigBuilder.setName(vpcEntity.getName());
            }

            if (vpcEntity.getCidr() != null) {
                vpcConfigBuilder.setCidr(vpcEntity.getCidr());
            }

            //set routes here

            Vpc.VpcState.Builder vpcStateBuilder = Vpc.VpcState.newBuilder();
            vpcStateBuilder.setOperationType(Common.OperationType.INFO);
            vpcStateBuilder.setConfiguration(vpcConfigBuilder.build());

            unicastGoalState.getGoalStateBuilder().addVpcStates(vpcStateBuilder.build());
        }
    }
}
