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
