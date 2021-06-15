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
