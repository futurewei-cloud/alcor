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
package com.futurewei.alcor.subnet.service.implement;

import com.futurewei.alcor.subnet.exception.PortWebJsonOrPortEntityIsNull;
import com.futurewei.alcor.subnet.service.SubnetToPortManagerService;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.PortWebJson;
import com.futurewei.alcor.web.entity.subnet.GatewayPortDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SubnetToPortManagerServiceImpl implements SubnetToPortManagerService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${microservices.port.service.url}")
    private String portUrl;

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public GatewayPortDetail createGatewayPort(String projectId, PortEntity portEntity) throws Exception {
        String portManagerServiceUrl = portUrl + "project/" + projectId + "/ports";
        HttpEntity<PortWebJson> portRequest = new HttpEntity<>(new PortWebJson(portEntity));
        PortWebJson portResponse = restTemplate.postForObject(portManagerServiceUrl, portRequest, PortWebJson.class);
        if (portResponse == null) {
            throw new PortWebJsonOrPortEntityIsNull();
        }

        PortEntity portEntityResponse = portResponse.getPortEntity();
        if (portEntityResponse == null) {
            throw new PortWebJsonOrPortEntityIsNull();
        }

        GatewayPortDetail gatewayPortDetail = new GatewayPortDetail(portEntityResponse.getMacAddress(), portEntityResponse.getId());

        return gatewayPortDetail;
    }

    @Override
    public void updateGatewayPort(String projectId, String portId, PortEntity portEntity) throws Exception {
        String portManagerServiceUrl = portUrl + "project/" + projectId + "/ports/" + portId;
        HttpEntity<PortWebJson> portRequest = new HttpEntity<>(new PortWebJson(portEntity));
        restTemplate.put(portManagerServiceUrl, portRequest, PortWebJson.class);
    }

    @Override
    public PortEntity getGatewayPortByPortID(String projectId, String portId) throws Exception {
        String portManagerServiceUrl = portUrl + "project/" + projectId + "/ports/" + portId;
        PortWebJson portResponse = restTemplate.getForObject(portManagerServiceUrl, PortWebJson.class);
        if (portResponse == null) {
            throw new PortWebJsonOrPortEntityIsNull();
        }

        PortEntity portEntityResponse = portResponse.getPortEntity();
        if (portEntityResponse == null) {
            throw new PortWebJsonOrPortEntityIsNull();
        }

        return portEntityResponse;
    }

    @Override
    public void deleteGatewayPort(String projectId, String portId) throws Exception {
        String portManagerServiceUrl = portUrl + "project/" + projectId + "/ports/" + portId;
        restTemplate.delete(portManagerServiceUrl);
    }

}
