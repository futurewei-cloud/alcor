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

package com.futurewei.alcor.portmanager.proxy;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.web.entity.elasticip.*;
import com.futurewei.alcor.web.restclient.ElasticIpManagerRestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticIpManagerProxy {

    private String projectId;

    private ElasticIpManagerRestClient elasticIpManagerRestClient;

    public ElasticIpManagerProxy(String projectId) {
        this.projectId = projectId;
        elasticIpManagerRestClient = SpringContextUtil.getBean(ElasticIpManagerRestClient.class);
    }

    /**
     * Update the elastic ip association info when port deleted or ip address deleted
     * @param arg1 Port id
     * @param arg2 Private ip
     * @return List<ElasticIpInfoWrapper></>
     * @throws Exception Rest request exception
     */
    public List<ElasticIpInfoWrapper> portIpDeleteEventProcess(Object arg1, Object arg2) throws Exception {
        String portId = (String)arg1;
        String ipAddress = (String)arg2;

        Map<String, Object[]> filters = new HashMap<>();
        filters.put("port_id", new Object[] {portId});
        if (ipAddress != null) {
            filters.put("elastic_ip", new Object[] {ipAddress});
        }

        List<ElasticIpInfoWrapper> result = new ArrayList<>();
        ElasticIpsInfoWrapper elasticIps = elasticIpManagerRestClient.getElasticIps(this.projectId, filters);
        if (elasticIps != null && elasticIps.getElasticips() != null) {
            for (ElasticIpInfo elasticIpInfo: elasticIps.getElasticips()) {
                ElasticIpInfo updateElasticIp = new ElasticIpInfo();
                updateElasticIp.setProjectId(this.projectId);
                updateElasticIp.setId(elasticIpInfo.getId());
                updateElasticIp.setPortId("");
                // Disassociate with those elastic ips
                result.add(elasticIpManagerRestClient.updateElasticIp(new ElasticIpInfoWrapper(updateElasticIp)));
            }
        }

        return result;
    }
}
