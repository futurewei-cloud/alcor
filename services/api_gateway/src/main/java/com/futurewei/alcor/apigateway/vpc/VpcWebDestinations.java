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

package com.futurewei.alcor.apigateway.vpc;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

@Data
@ConfigurationProperties(prefix = "vpc.destinations")
public class VpcWebDestinations {

    private String defaultServiceUrl = "http://192.168.1.17:30001"; // Kubernetes
    //private String defaultServiceUrl = "http://192.168.137.1:8080"; // DOcker

    @NotNull
    private String vpcManagerServiceUrl;

    public String getVpcManagerServiceUrl() {
        return this.vpcManagerServiceUrl == null ? defaultServiceUrl : this.vpcManagerServiceUrl;
    }

}
