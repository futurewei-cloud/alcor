/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.futurewei.alcor.web.entity.mac;

import java.util.BitSet;

public class MacRangePartition {

    private String id;
    private String rangeId;
    private final int partition;
    private long start;
    private long end;
    private int total;
    private BitSet bitSet;

    public MacRangePartition(String rangeId, int partition, long start, long end) {
        this.id = rangeId + "_" + partition;
        this.rangeId = rangeId;
        this.partition = partition;
        this.start = start;
        this.end = end;
        this.total = (int) (end - start);
        this.bitSet = new BitSet();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRangeId() {
        return rangeId;
    }

    public void setRangeId(String rangeId) {
        this.rangeId = rangeId;
    }

    public int getPartition() {
        return partition;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public int getTotal() {
        return total;
    }

    public BitSet getBitSet() {
        return bitSet;
    }
}
