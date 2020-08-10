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

import org.springframework.web.client.RestTemplate;

import java.util.List;

abstract class AbstractRestClient {
    RestTemplate restTemplate = new RestTemplate();

    <T> T getForObject(String url, Class<T> tClass) throws Exception {
        T response = restTemplate.getForObject(url, tClass);
        if (response == null) {
            throw new Exception("Get request failed, url:" + url);
        }

        return response;
    }

    protected String buildQueryParameter(String key, List<String> values) {
        values.forEach(v -> v = key + "=" + v);
        return String.join("&", values);
    }
}
