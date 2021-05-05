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
import com.futurewei.alcor.portmanager.processor.PortContext;
import com.futurewei.alcor.web.entity.dataplane.InternalDPMResultList;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.restclient.DataPlaneManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateNetworkConfigRequest extends AbstractRequest {
    private static final Logger LOG = LoggerFactory.getLogger(CreateNetworkConfigRequest.class);

    private DataPlaneManagerRestClient dataPlaneManagerRestClient;
    private NetworkConfiguration networkConfiguration;
    public InternalDPMResultList resultList;

    public CreateNetworkConfigRequest(PortContext context, NetworkConfiguration networkConfiguration) {
        super(context);
        this.networkConfiguration = networkConfiguration;
        this.dataPlaneManagerRestClient = SpringContextUtil.getBean(DataPlaneManagerRestClient.class);
    }

    public InternalDPMResultList getResultList() {
        return resultList;
    }

    @Override
    public void send() throws Exception {
        resultList = dataPlaneManagerRestClient.createNetworkConfig(networkConfiguration);
    }

    @Override
    public void rollback() throws Exception {
        LOG.info("CreateNetworkConfigRequest rollback, networkConfiguration: {}", networkConfiguration);
        dataPlaneManagerRestClient.deleteNetworkConfig(networkConfiguration);
    }
}
