package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class InternalRouteTable {
    @JsonProperty("routes")
    private List<InternalRouteEntry> routes;
}
