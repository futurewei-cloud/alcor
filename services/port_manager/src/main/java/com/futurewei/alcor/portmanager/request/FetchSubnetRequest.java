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
package com.futurewei.alcor.portmanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.exception.GetSubnetEntityException;
import com.futurewei.alcor.portmanager.processor.PortContext;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.entity.subnet.SubnetsWebJson;
import com.futurewei.alcor.web.restclient.SubnetManagerRestClient;

import java.util.ArrayList;
import java.util.List;

public class FetchSubnetRequest extends AbstractRequest {
    private SubnetManagerRestClient subnetManagerRestClient;
    private List<String> subnetIds;
    private List<SubnetEntity> subnetEntities;
    private boolean allocateIpAddress;

    public FetchSubnetRequest(PortContext context, List<String> subnetIds, boolean allocateIpAddress) {
        super(context);
        this.subnetIds = subnetIds;
        this.allocateIpAddress = allocateIpAddress;
        this.subnetEntities = new ArrayList<>();
        subnetManagerRestClient = SpringContextUtil.getBean(SubnetManagerRestClient.class);
    }

    public List<SubnetEntity> getSubnetEntities() {
        return subnetEntities;
    }

    public boolean isAllocateIpAddress() {
        return allocateIpAddress;
    }

    @Override
    public void send() throws Exception {
        if (subnetIds.size() == 0){
            return;
        }
        String projectId = context.getProjectId();
        if (subnetIds.size() == 1) {
            SubnetWebJson subnetWebJson = subnetManagerRestClient.getSubnet(projectId, subnetIds.get(0));
            if (subnetWebJson == null || subnetWebJson.getSubnet() == null) {
                throw new GetSubnetEntityException();
            }

            subnetEntities.add(subnetWebJson.getSubnet());
        } else {
            SubnetsWebJson subnetsWebJson = subnetManagerRestClient.getSubnetBulk(projectId, subnetIds);
            if (subnetsWebJson == null ||
                    subnetsWebJson.getSubnets() == null ||
                    subnetsWebJson.getSubnets().size() != subnetIds.size()) {
                throw new GetSubnetEntityException();
            }

            subnetEntities.addAll(subnetsWebJson.getSubnets());
        }
    }

    @Override
    public void rollback() {

    }
}
