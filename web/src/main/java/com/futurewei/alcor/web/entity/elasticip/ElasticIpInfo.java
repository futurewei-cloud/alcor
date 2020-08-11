/*
Copyright 2020 The Alcor Authors.

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

package com.futurewei.alcor.web.entity.elasticip;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class ElasticIpInfo extends ElasticIp {

    @JsonProperty("state")
    private String state;

    public ElasticIpInfo() {
    }

    public ElasticIpInfo(ElasticIp eip) {
        super(eip);
        if (eip.getPortId() != null) {
            this.state = ElasticIpState.ACTIVATED.getState();
        } else {
            this.state = ElasticIpState.DEACTIVATED.getState();
        }
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "ElasticIpInfo{" +
                "state='" + state + '\'' +
                "} " + super.toString();
    }
}
