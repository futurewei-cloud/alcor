package com.futurewei.alcor.web.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.exception.KeyInvalidException;
import com.futurewei.alcor.common.exception.VlanKeyAllocNotFoundException;
import com.futurewei.alcor.web.allocator.KeyAllocator;
import com.futurewei.alcor.web.allocator.VlanKeyAllocator;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.HashMap;
import java.util.Map;

@Data
public class NetworkVlanType {

    @Id
    private Long key; // 1 - 4094

    @JsonProperty("vlan_id")
    private String vlanId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("segment_id")
    private String segmentId;

    @JsonProperty("mtu")
    private String mtu; // 1500 - 18190

    @JsonProperty("status")
    private String status; // active/suspend

    @CreatedDate
    @JsonProperty("created_at")
    private String created_at;

    @LastModifiedDate
    @JsonProperty("updated_at")
    private String updated_at;

    public NetworkVlanType() { }

}
