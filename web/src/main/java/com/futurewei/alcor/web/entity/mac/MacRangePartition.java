/*
 *
 * Copyright 2019 The Alcor Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * /
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
