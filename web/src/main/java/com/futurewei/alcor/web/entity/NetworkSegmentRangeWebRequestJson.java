package com.futurewei.alcor.web.entity;

import lombok.Data;

import java.util.UUID;

@Data
public class NetworkSegmentRangeWebRequestJson {

    private NetworkSegmentRangeWebRequestObject network_segment_range;

    public NetworkSegmentRangeWebRequestJson() {

    }

    public NetworkSegmentRangeWebRequestJson(NetworkSegmentRangeWebRequestObject segmentRange) {
        this.network_segment_range = segmentRange;
    }

    public NetworkSegmentRangeWebRequestJson(NetworkSegmentRangeWebRequestObject segmentRange, UUID genId) {
        this.network_segment_range = segmentRange;
        this.network_segment_range.setId(String.valueOf(genId));
    }

}
