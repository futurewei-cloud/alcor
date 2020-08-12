package com.futurewei.alcor.web.entity.vpc;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SegmentsWebResponseJson {

    private ArrayList<SegmentEntity> segments;

    public SegmentsWebResponseJson() {
    }

    public SegmentsWebResponseJson(List<SegmentEntity> segments) {
        this.segments = new ArrayList<>(segments);
    }

}
