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

package com.futurewei.alcor.apigateway.filter;

import com.futurewei.alcor.apigateway.client.KeystoneClient;
import com.futurewei.alcor.common.db.CacheFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value="com.futurewei.alcor.common.db")
public class KeystoneConfiguration {

    @Value("${keystone.enable}")
    private boolean keystoneEnable;

    @Autowired
    private CacheFactory cacheFactory;

    @Bean
    public KeystoneAuthWebFilter keystoneAuthWebFilter(){
        if(!keystoneEnable){
            return null;
        }
        return new KeystoneAuthWebFilter();
    }

    @Bean
    public KeystoneClient keystoneClient(){
        if(!keystoneEnable){
            return null;
        }
        return new KeystoneClient(cacheFactory);
    }
}
