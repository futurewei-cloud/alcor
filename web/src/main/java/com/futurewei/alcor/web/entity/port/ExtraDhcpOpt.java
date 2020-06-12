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
package com.futurewei.alcor.web.entity.port;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExtraDhcpOpt {
    @JsonProperty("ip_version")
    private String ipVersion;

    @JsonProperty("opt_name")
    private String optName;

    @JsonProperty("opt_value")
    private String optValue;

    public ExtraDhcpOpt() {

    }

    public ExtraDhcpOpt(String ipVersion, String optName, String optValue) {
        this.ipVersion = ipVersion;
        this.optName = optName;
        this.optValue = optValue;
    }

    public String getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(String ipVersion) {
        this.ipVersion = ipVersion;
    }

    public String getOptName() {
        return optName;
    }

    public void setOptName(String optName) {
        this.optName = optName;
    }

    public String getOptValue() {
        return optValue;
    }

    public void setOptValue(String optValue) {
        this.optValue = optValue;
    }
}
