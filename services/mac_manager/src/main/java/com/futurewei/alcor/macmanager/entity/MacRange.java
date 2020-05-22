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
package com.futurewei.alcor.macmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
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

    @JsonIgnore
    private BitSet bitSet;

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
        this.bitSet = new BitSet();

    }

    public MacRange(String rangeId, String from, String to, String state, BitSet bitset) {
        this.rangeId = rangeId;
        this.from = from;
        this.to = to;
        this.state = state;
        this.bitSet = bitset;
    }
}


