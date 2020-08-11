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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.ControllerUtil;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpInfo;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpInfoWrapper;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpsInfoWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Configuration
public class ElasticIpManagerRestClient extends AbstractRestClient {
    @Value("${microservices.elasticip.service.url:#{\"\"}}")
    private String elasticIpManagerUrl;

    @DurationStatistics
    public ElasticIpInfoWrapper updateElasticIp(ElasticIpInfoWrapper elasticIpInfoWrapper) throws Exception {
        ElasticIpInfo elasticIpInfo = elasticIpInfoWrapper.getElasticip();
        String url = elasticIpManagerUrl + "/project/" + elasticIpInfo.getProjectId() + "/elasticip/" +
                elasticIpInfo.getId();

        try {
            HttpHeaders headers = new HttpHeaders();
            MimeType mimeType = MimeTypeUtils.parseMimeType("application/json");
            MediaType mediaType = new MediaType(mimeType.getType(), mimeType.getSubtype());
            headers.setContentType(mediaType);

            ObjectMapper mapper = new ObjectMapper();
            HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(elasticIpInfoWrapper), headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<ElasticIpInfoWrapper> resultEntity = restTemplate.exchange(
                    url, HttpMethod.PUT, entity, ElasticIpInfoWrapper.class);
            return resultEntity.getBody();
        } catch (RestClientException e) {
            throw new Exception("update elastic ip exception");
        }
    }

    @DurationStatistics
    public ElasticIpsInfoWrapper getElasticIps(String projectId, Map<String, Object[]> filters) throws Exception {
        String url = elasticIpManagerUrl + "/project/" + projectId + "/elasticips" +
                ControllerUtil.transformParamsToUrl(filters);
        ElasticIpsInfoWrapper elasticIpsInfoWrapper = null;

        try {
            elasticIpsInfoWrapper = restTemplate.getForObject(url, ElasticIpsInfoWrapper.class);
        } catch (RestClientException e) {
            throw new Exception("Get elastic ips exception");
        }

        return elasticIpsInfoWrapper;
    }

}
