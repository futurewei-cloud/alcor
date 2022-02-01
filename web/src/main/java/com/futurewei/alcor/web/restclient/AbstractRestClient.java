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
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Import(RestTemplateConfig.class)
abstract class AbstractRestClient {
    RestTemplate restTemplate;

    public AbstractRestClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public AbstractRestClient(RestTemplateBuilder restTemplateBuilder, int connectTimeout, int readTimeout) {
        if (connectTimeout == 100) {
            this.restTemplate = restTemplateBuilder.build();
        } else {
            this.restTemplate = restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(connectTimeout)).setReadTimeout(Duration.ofSeconds(readTimeout)).build();
        }

    }

    <T> T getForObject(String url, Class<T> tClass) throws Exception {
        T response = restTemplate.getForObject(url, tClass);
        if (response == null) {
            throw new Exception("Get request failed, url:" + url);
        }

        return response;
    }

    protected String buildQueryParameter(String key, List<String> values) {
        List<String> params = values.stream().map(v -> key + "=" + v).collect(Collectors.toList());
        return String.join("&", params);
    }
}
