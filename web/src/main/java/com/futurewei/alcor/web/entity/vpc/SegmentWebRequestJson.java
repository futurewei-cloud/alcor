package com.futurewei.alcor.web.entity.vpc;

import lombok.Data;

import java.util.UUID;

@Data
public class SegmentWebRequestJson {

    private SegmentWebRequest segment;

    public SegmentWebRequestJson() {

    }

    public SegmentWebRequestJson(SegmentWebRequest segment) {
        this.segment = segment;
    }

    public SegmentWebRequestJson(SegmentWebRequest segment, UUID genId) {
        this.segment = segment;
        this.segment.setId(String.valueOf(genId));
    }

}
