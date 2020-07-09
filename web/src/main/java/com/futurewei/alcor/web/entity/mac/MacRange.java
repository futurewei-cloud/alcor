/*Copyright 2019 The Alcor Authors.

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
package com.futurewei.alcor.web.entity.mac;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.security.DrbgParameters;
import java.util.BitSet;

@Data
public class MacRange implements Serializable {
    @JsonProperty("range_id")
    private String rangeId;

    @JsonProperty("from")
    private String from;

    @JsonProperty("to")
    private String to;

    @JsonProperty("state")
    private String state;

    private long capacity;
//    @JsonIgnore
//    private BitSet bitSet;

    public MacRange() {
    }

    public MacRange(MacRange range) {
        this(range.rangeId, range.from, range.to, range.state);
    }

    public MacRange(String rangeId, String from, String to, String state) {
        this.rangeId = rangeId;
        this.from = from;
        this.to = to;
        this.state = state;
//        this.bitSet = new BitSet();
        this.capacity = Long.valueOf(to.replaceAll(MacAddress.MAC_DELIMITER, ""), 16)
                - Long.valueOf(from.replaceAll(MacAddress.MAC_DELIMITER, ""), 16);
    }

//    public MacRange(String rangeId, String from, String to, String state, BitSet bitset) {
//        this.rangeId = rangeId;
//        this.from = from;
//        this.to = to;
//        this.state = state;
//        this.bitSet = bitset;
//    }


    public String getRangeId() {
        return rangeId;
    }

    public void setRangeId(String rangeId) {
        this.rangeId = rangeId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getCapacity() {
        return capacity;
    }
}


