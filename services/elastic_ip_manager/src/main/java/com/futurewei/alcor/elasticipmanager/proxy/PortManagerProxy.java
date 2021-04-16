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

package com.futurewei.alcor.elasticipmanager.proxy;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.elasticipmanager.exception.elasticip.ElasticIpGetPortException;
import com.futurewei.alcor.elasticipmanager.exception.elasticip.ElasticIpPipNotFound;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.PortWebJson;
import com.futurewei.alcor.web.restclient.PortManagerRestClient;


public class PortManagerProxy {

    private PortManagerRestClient portManagerRestClient;
    private String projectId;

    public PortManagerProxy(String projectId) {
        portManagerRestClient = SpringContextUtil.getBean(PortManagerRestClient.class);
        this.projectId = projectId;
    }

    /**
     * Get Port by id
     * @param arg1 Port id
     * @return PortEntity
     * @throws Exception Rest request exception
     */
    public PortEntity getPortById(Object arg1) throws Exception {
        String portId = (String)arg1;

        PortWebJson portWebJson;
        try {
            portWebJson = portManagerRestClient.getPort(this.projectId, portId);
        } catch (Exception e) {
            throw new ElasticIpGetPortException();
        }

        if (portWebJson == null || portWebJson.getPortEntity() == null) {
            throw new ElasticIpPipNotFound();
        }

        return portWebJson.getPortEntity();
    }
}
