package com.futurewei.alcor.web.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SegmentsWebResponseJson {

    private ArrayList<SegmentWebResponseObject> segments;

    public SegmentsWebResponseJson() {
    }

    public SegmentsWebResponseJson(List<SegmentWebResponseObject> segments) {
        this.segments = new ArrayList<>(segments);
    }

}
