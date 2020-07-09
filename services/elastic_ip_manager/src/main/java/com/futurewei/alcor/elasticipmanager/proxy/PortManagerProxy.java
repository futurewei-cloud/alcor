package com.futurewei.alcor.elasticipmanager.proxy;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.web.restclient.IpManagerRestClient;


public class PortManagerProxy {

    private IpManagerRestClient ipManagerRestClient;
    private String projectId;

    public PortManagerProxy(String projectId) {
        ipManagerRestClient = SpringContextUtil.getBean(IpManagerRestClient.class);
        this.projectId = projectId;

        // todo port rest client
    }

}
