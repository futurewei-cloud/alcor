/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.portmanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.exception.GetVpcEntityException;
import com.futurewei.alcor.portmanager.processor.PortContext;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;
import com.futurewei.alcor.web.restclient.VpcManagerRestClient;

import java.util.ArrayList;
import java.util.List;

public class FetchVpcRequest extends AbstractRequest {
    private VpcManagerRestClient vpcManagerRestClient;
    private List<String> vpcIds;
    private List<VpcEntity> vpcEntities;

    public FetchVpcRequest(PortContext context, List<String> vpcIds) {
        super(context);
        this.vpcIds = vpcIds;
        this.vpcEntities = new ArrayList<>();
        this.vpcManagerRestClient = SpringContextUtil.getBean(VpcManagerRestClient.class);
    }

    public List<VpcEntity> getVpcEntities() {
        return vpcEntities;
    }

    @Override
    public void send() throws Exception {
        //TODO: Instead by getVpcsByVpcIds interface
        for (String vpcId: vpcIds) {
            VpcWebJson vpcWebJson = vpcManagerRestClient.getVpc(context.getProjectId(), vpcId);
            if (vpcWebJson == null || vpcWebJson.getNetwork() == null) {
                throw new GetVpcEntityException();
            }

            vpcEntities.add(vpcWebJson.getNetwork());
        }
    }

    @Override
    public void rollback() {

    }
}
