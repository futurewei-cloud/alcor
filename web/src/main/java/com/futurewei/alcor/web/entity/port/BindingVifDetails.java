package com.futurewei.alcor.web.entity.port;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class BindingVifDetails implements Serializable {
    @JsonProperty("ovs_hybrid_plug")
    private boolean ovsHybridPlug;
    @JsonProperty("port_filter")
    private boolean portFilter;
}
