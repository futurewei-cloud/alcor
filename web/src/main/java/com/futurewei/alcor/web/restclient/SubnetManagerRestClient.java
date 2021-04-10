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
