package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import lombok.Data;

@Data
public class SubnetEntityNB extends SubnetEntity {
    @JsonProperty("tunnel_id")
    private Long tunnelId;
}
