package com.futurewei.alcor.web.entity;

import lombok.Data;

import java.util.UUID;

@Data
public class SegmentWebResponseJson {

    private SegmentWebResponseObject segment;

    public SegmentWebResponseJson() {

    }

    public SegmentWebResponseJson(SegmentWebResponseObject segment) {
        this.segment = segment;
    }

    public SegmentWebResponseJson(SegmentWebResponseObject segment, UUID genId) {
        this.segment = segment;
        this.segment.setId(String.valueOf(genId));

    }
}
