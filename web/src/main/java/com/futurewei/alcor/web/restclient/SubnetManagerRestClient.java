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
package com.futurewei.alcor.web.restclient;

import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.subnet.SubnetWebJson;
import com.futurewei.alcor.web.entity.subnet.SubnetsWebJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SubnetManagerRestClient extends AbstractRestClient {
    @Value("${microservices.subnet.service.url:#{\"\"}}")
    private String subnetManagerUrl;

    @DurationStatistics
    public SubnetWebJson getSubnet(String projectId, String subnetId) throws Exception {
        String url = subnetManagerUrl + "/project/" + projectId + "/subnets/" + subnetId;

        SubnetWebJson subnetWebJson = restTemplate.getForObject(url, SubnetWebJson.class);
        if (subnetWebJson == null) {
            throw new Exception("Get subnet failed");
        }

        return subnetWebJson;
    }

    @DurationStatistics
    public SubnetsWebJson getSubnetBulk(String projectId, List<String> subnetIds) throws Exception {
        String queryParameter = buildQueryParameter("id", subnetIds);
        String url = subnetManagerUrl + "/project/" + projectId + "/subnets?" + queryParameter;

        SubnetsWebJson subnetsWebJson = restTemplate.getForObject(url, SubnetsWebJson.class);
        if (subnetsWebJson == null) {
            throw new Exception("Get subnets failed");
        }

        return subnetsWebJson;
    }
}
