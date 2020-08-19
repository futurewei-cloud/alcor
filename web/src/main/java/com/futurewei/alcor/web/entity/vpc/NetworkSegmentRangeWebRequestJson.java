package com.futurewei.alcor.web.entity.vpc;

import lombok.Data;

import java.util.UUID;

@Data
public class NetworkSegmentRangeWebRequestJson {

    private NetworkSegmentRangeWebRequest network_segment_range;

    public NetworkSegmentRangeWebRequestJson() {

    }

    public NetworkSegmentRangeWebRequestJson(NetworkSegmentRangeWebRequest segmentRange) {
        this.network_segment_range = segmentRange;
    }

    public NetworkSegmentRangeWebRequestJson(NetworkSegmentRangeWebRequest segmentRange, UUID genId) {
        this.network_segment_range = segmentRange;
        this.network_segment_range.setId(String.valueOf(genId));
    }

}
