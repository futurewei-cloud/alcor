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
package com.futurewei.alcor.macmanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MacStateJson {

    @JsonProperty("mac_state")
    private MacState macState;

    public MacStateJson() {

    }

    public MacStateJson(MacState macState) {
        this.macState = macState;
    }

    public MacState getMacState() {
        return macState;
    }

    public void setMacState(MacState macState) {
        this.macState = macState;
    }
}

