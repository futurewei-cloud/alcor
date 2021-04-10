/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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


