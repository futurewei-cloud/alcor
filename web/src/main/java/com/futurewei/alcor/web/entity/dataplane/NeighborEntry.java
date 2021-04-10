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

package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NeighborEntry {
    @JsonProperty("neighbor_type")
    private NeighborType neighborType;

    @JsonProperty("local_ip")
    private String localIp;

    @JsonProperty("neighbor_ip")
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
