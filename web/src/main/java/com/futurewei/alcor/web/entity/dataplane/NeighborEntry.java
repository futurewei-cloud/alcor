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
package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class NeighborEntry {
    @JsonProperty("neighbor_type")
    @SerializedName("neighbor_type")
    private NeighborType neighborType;

    @JsonProperty("local_ip")
    @SerializedName("local_ip")
    private String localIp;

    @JsonProperty("neighbor_ip")
    @SerializedName("neighbor_ip")
    private String neighborIp;

    public enum NeighborType {
        L2("L2"),
        L3("L3");

        private String type;

        NeighborType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public NeighborEntry() {

    }

    public NeighborEntry(NeighborType neighborType, String localIp, String neighborIp) {
        this.neighborType = neighborType;
        this.localIp = localIp;
        this.neighborIp = neighborIp;
    }

    public NeighborType getNeighborType() {
        return neighborType;
    }

    public void setNeighborType(NeighborType neighborType) {
        this.neighborType = neighborType;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public String getNeighborIp() {
        return neighborIp;
    }

    public void setNeighborIp(String neighborIp) {
        this.neighborIp = neighborIp;
    }
}
