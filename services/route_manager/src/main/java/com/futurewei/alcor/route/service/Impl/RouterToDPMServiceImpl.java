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
package com.futurewei.alcor.route.service.Impl;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.route.config.ConstantsConfig;
import com.futurewei.alcor.route.exception.DPMFailedHandleRequest;
import com.futurewei.alcor.route.service.RouterToDPMService;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.web.entity.dataplane.InternalDPMResultList;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.restclient.DataPlaneManagerRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@ComponentScan(value = "com.futurewei.alcor.common.utils")
@ComponentScan(value = "com.futurewei.alcor.web.restclient")
public class RouterToDPMServiceImpl implements RouterToDPMService {

    @Autowired
    private DataPlaneManagerRestClient dataPlaneManagerRestClient;

    @Override
    public void sendInternalRouterInfoToDPM(InternalRouterInfo internalRouterInfo) throws Exception{
        NetworkConfiguration networkConfiguration = new NetworkConfiguration();
        List<InternalRouterInfo> internalRouterInfos = new ArrayList<>();
        internalRouterInfos.add(internalRouterInfo);
        networkConfiguration.setInternalRouterInfos(internalRouterInfos);
        networkConfiguration.setRsType(Common.ResourceType.ROUTER);
        networkConfiguration.setOpType(Common.OperationType.valueOf(internalRouterInfo.getOperationType().name()));
        InternalDPMResultList dpmResponse = null;
        
        switch (internalRouterInfo.getOperationType())
        {
            case UPDATE:
                dpmResponse = dataPlaneManagerRestClient.updateNetworkConfig(networkConfiguration);
            case DELETE:
                dataPlaneManagerRestClient.deleteNetworkConfig(networkConfiguration);
            default:
        }

        if (dpmResponse == null) {
            throw new DPMFailedHandleRequest();
        }
        String resultMessage = dpmResponse.getResultMessage();
        if (resultMessage.equals(ConstantsConfig.DPMFailedHandleRequest)) {
            throw new DPMFailedHandleRequest();
        }
    }
}
