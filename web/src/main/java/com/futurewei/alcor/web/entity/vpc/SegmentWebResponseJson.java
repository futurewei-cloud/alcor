package com.futurewei.alcor.web.entity.vpc;

import lombok.Data;

import java.util.UUID;

@Data
public class SegmentWebResponseJson {

    private SegmentEntity segment;

    public SegmentWebResponseJson() {

    }

    public SegmentWebResponseJson(SegmentEntity segment) {
        this.segment = segment;
    }

    public SegmentWebResponseJson(SegmentEntity segment, UUID genId) {
        this.segment = segment;
        this.segment.setId(String.valueOf(genId));

    }
}
