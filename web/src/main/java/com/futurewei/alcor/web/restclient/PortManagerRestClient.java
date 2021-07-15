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

import com.futurewei.alcor.common.http.RestTemplateConfig;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.port.PortWebJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClientException;

@Configuration
@Import(RestTemplateConfig.class)
public class PortManagerRestClient  extends AbstractRestClient {
    @Value("${microservices.port.service.url:#{\"\"}}")
    private String portManagerUrl;

    public PortManagerRestClient(RestTemplateBuilder restTemplateBuilder) {
        super(restTemplateBuilder);
    }

    @DurationStatistics
    public PortWebJson getPort(String projectId, String portId) throws Exception {
        String url = portManagerUrl + "/project/" + projectId + "/ports/" + portId;
        PortWebJson portWebJson = null;

        try {
            portWebJson = restTemplate.getForObject(url, PortWebJson.class);
        } catch (RestClientException e) {
            throw new Exception("Get port exception");
        }

        return portWebJson;
    }
}
