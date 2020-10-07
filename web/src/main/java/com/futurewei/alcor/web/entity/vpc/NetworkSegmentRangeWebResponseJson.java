package com.futurewei.alcor.web.entity.vpc;

import lombok.Data;

import java.util.UUID;

@Data
public class NetworkSegmentRangeWebResponseJson {

    private NetworkSegmentRangeEntity network_segment_range;

    public NetworkSegmentRangeWebResponseJson() {

    }

    public NetworkSegmentRangeWebResponseJson(NetworkSegmentRangeEntity segmentRange) {
        this.network_segment_range = segmentRange;
    }

    public NetworkSegmentRangeWebResponseJson(NetworkSegmentRangeEntity segmentRange, UUID genId) {
        this.network_segment_range = segmentRange;
        this.network_segment_range.setId(String.valueOf(genId));
    }

}
