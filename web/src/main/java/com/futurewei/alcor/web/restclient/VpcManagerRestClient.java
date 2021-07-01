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
package com.futurewei.alcor.web.restclient;

import com.futurewei.alcor.common.http.RestTemplateConfig;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.vpc.VpcWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcsWebJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

@Configuration
@Import(RestTemplateConfig.class)
public class VpcManagerRestClient extends AbstractRestClient {
    @Value("${microservices.vpc.service.url:#{\"\"}}")
    private String vpcManagerUrl;

    public VpcManagerRestClient(RestTemplateBuilder restTemplateBuilder) {
        super(restTemplateBuilder);
    }

    @DurationStatistics
    public VpcWebJson getVpc(String projectId, String vpcId) throws Exception {
        String url = vpcManagerUrl + "/project/" + projectId + "/vpcs/" + vpcId;
        VpcWebJson vpcWebJson = restTemplate.getForObject(url, VpcWebJson.class);
        if (vpcWebJson == null) {
            throw new Exception("Get vpc failed");
        }

        return vpcWebJson;
    }

    @DurationStatistics
    public VpcsWebJson getVpcBulk(String projectId, List<String> vpcIds) throws Exception {
        String queryParameter = buildQueryParameter("id", vpcIds);
        String url = vpcManagerUrl + "/project/" + projectId + "/vpcs?" + queryParameter;

        VpcsWebJson vpcsWebJson = restTemplate.getForObject(url, VpcsWebJson.class);
        if (vpcsWebJson == null) {
            throw new Exception("Get vpcs failed");
        }

        return vpcsWebJson;
    }
}
