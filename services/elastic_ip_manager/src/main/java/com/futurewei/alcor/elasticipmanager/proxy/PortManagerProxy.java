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
