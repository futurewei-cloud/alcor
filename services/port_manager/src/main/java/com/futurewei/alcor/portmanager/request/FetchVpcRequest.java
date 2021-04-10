/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.portmanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.exception.GetVpcEntityException;
import com.futurewei.alcor.portmanager.processor.PortContext;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcsWebJson;
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
        //The performance of getVpc() is better than getVpcs().
        if (vpcIds.size() == 1) {
            VpcWebJson vpcWebJson = vpcManagerRestClient.getVpc(context.getProjectId(), vpcIds.get(0));
            if (vpcWebJson == null || vpcWebJson.getNetwork() == null) {
                throw new GetVpcEntityException();
            }

            vpcEntities.add(vpcWebJson.getNetwork());
        } else {
            VpcsWebJson vpcsWebJson = vpcManagerRestClient.getVpcBulk(context.getProjectId(), vpcIds);
            if (vpcsWebJson == null ||
                    vpcsWebJson.getVpcs() == null ||
                    vpcsWebJson.getVpcs().size() != vpcIds.size()) {
                throw new GetVpcEntityException();
            }

            vpcEntities.addAll(vpcsWebJson.getVpcs());
        }
    }

    @Override
    public void rollback() {

    }
}
