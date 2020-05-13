package com.futurewei.alcor.web.entity;

import lombok.Data;

import java.util.UUID;

@Data
public class NetworkSegmentRangeWebResponseJson {

    private NetworkSegmentRangeWebResponseObject network_segment_range;

    public NetworkSegmentRangeWebResponseJson() {

    }

    public NetworkSegmentRangeWebResponseJson(NetworkSegmentRangeWebResponseObject segmentRange) {
        this.network_segment_range = segmentRange;
    }

    public NetworkSegmentRangeWebResponseJson(NetworkSegmentRangeWebResponseObject segmentRange, UUID genId) {
        this.network_segment_range = segmentRange;
        this.network_segment_range.setId(String.valueOf(genId));
    }

}
