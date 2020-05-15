package com.futurewei.alcor.web.entity.subnet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AllocationPool {

    @JsonProperty("start")
    private String start;

    @JsonProperty("end")
    private String end;

    public AllocationPool () {}

    public AllocationPool(String start, String end) {
        this.start = start;
        this.end = end;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}
