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

import com.futurewei.alcor.portmanager.request.FetchVpcRequest;
import com.futurewei.alcor.portmanager.request.IRestRequest;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

import java.util.*;
import java.util.stream.Collectors;

@AfterProcessor(PortProcessor.class)
public class VpcProcessor extends AbstractProcessor {
    private void fetchVpcListCallback(IRestRequest request) {
        List<VpcEntity> vpcEntities = ((FetchVpcRequest) request).getVpcEntities();
        request.getContext().getNetworkConfig().setVpcEntities(vpcEntities);
    }

    private void getVpcEntities(PortContext context, List<PortEntity> portEntities) {
        Set<String> vpcIds = portEntities
                .stream()
                .filter(p -> p.getVpcId() != null)
                .map(PortEntity::getVpcId)
                .collect(Collectors.toSet());

        if (vpcIds.size() > 0) {
            IRestRequest fetchVpcRequest = new FetchVpcRequest(context, new ArrayList<>(vpcIds));
            context.getRequestManager().sendRequestAsync(fetchVpcRequest, this::fetchVpcListCallback);
        }
    }

    @Override
    void createProcess(PortContext context) {
        getVpcEntities(context, context.getPortEntities());
    }

    @Override
    void updateProcess(PortContext context) {
        PortEntity portEntity = context.getNewPortEntity();
        getVpcEntities(context, Collections.singletonList(portEntity));
    }

    @Override
    void deleteProcess(PortContext context) {
        getVpcEntities(context, context.getPortEntities());
    }
}
