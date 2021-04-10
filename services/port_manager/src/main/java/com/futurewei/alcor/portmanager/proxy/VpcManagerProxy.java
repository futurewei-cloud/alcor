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

package com.futurewei.alcor.portmanager.proxy;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.exception.GetVpcEntityException;
import com.futurewei.alcor.web.entity.port.*;
import com.futurewei.alcor.web.entity.vpc.*;
import com.futurewei.alcor.web.restclient.VpcManagerRestClient;
import com.futurewei.alcor.portmanager.rollback.Rollback;
import java.util.Stack;

public class VpcManagerProxy {
    private VpcManagerRestClient vpcManagerRestClient;
    private Stack<Rollback> rollbacks;

    public VpcManagerProxy(Stack<Rollback> rollbacks) {
        vpcManagerRestClient = SpringContextUtil.getBean(VpcManagerRestClient.class);
        this.rollbacks = rollbacks;
    }

    /**
     * Verify if the vpc of vpcId exists
     * @param args PortEntity
     * @return The information of vpc
     * @throws Exception Rest request exception
     */
    public VpcEntity getVpcEntity(Object args) throws Exception {
        PortEntity portEntity = (PortEntity)args;
        VpcWebJson vpcWebJson = vpcManagerRestClient.getVpc(portEntity.getProjectId(), portEntity.getVpcId());
        if (vpcWebJson == null || vpcWebJson.getNetwork() == null) {
            throw new GetVpcEntityException();
        }

        return vpcWebJson.getNetwork();
    }
}
