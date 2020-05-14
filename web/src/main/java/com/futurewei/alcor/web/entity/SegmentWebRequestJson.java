package com.futurewei.alcor.web.entity;

import lombok.Data;

import java.util.UUID;

@Data
public class SegmentWebRequestJson {

    private SegmentWebRequestObject segment;

    public SegmentWebRequestJson() {

    }

    public SegmentWebRequestJson(SegmentWebRequestObject segment) {
        this.segment = segment;
    }

    public SegmentWebRequestJson(SegmentWebRequestObject segment, UUID genId) {
        this.segment = segment;
        this.segment.setId(String.valueOf(genId));
    }

}
