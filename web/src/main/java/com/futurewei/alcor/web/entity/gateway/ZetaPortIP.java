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
package com.futurewei.alcor.web.entity.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ZetaPortIP {
    @JsonProperty("ip")
    private String ip;

    @JsonProperty("vip")
    private String vip;

    public ZetaPortIP() {

    }

    public ZetaPortIP(String ip, String vip) {
        this.ip = ip;
        this.vip = vip;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getVip() {
        return this.vip;
    }

    public void setVip(String vip) {
        this.vip = vip;
    }
}
