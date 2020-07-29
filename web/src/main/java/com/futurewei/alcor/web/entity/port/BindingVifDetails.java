package com.futurewei.alcor.web.entity.port;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BindingVifDetails {
    @JsonProperty("ovs_hybrid_plug")
    private boolean ovsHybridPlug;
    @JsonProperty("port_filter")
    private boolean portFilter;
}
