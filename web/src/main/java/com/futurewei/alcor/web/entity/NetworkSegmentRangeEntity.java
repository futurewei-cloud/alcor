package com.futurewei.alcor.web.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.List;
import java.util.Map;

@Data
public class NetworkSegmentRangeEntity extends CustomerResource {

    @JsonProperty("is_default")
    private boolean isDefault;

    @JsonProperty("minimum")
    private Integer minimum;

    @JsonProperty("maximum")
    private Integer maximum;

    @JsonProperty("tags")
    private String tags;

    @JsonProperty("network_type")
    private String networkType;

    @JsonProperty("physical_network")
    private String physicalNetwork;

    @JsonProperty("shared")
    private Boolean shared;

    @JsonProperty("revision_number")
    private Integer revisionNumber;

    @JsonProperty("available")
    private List<Integer> available;

    @JsonProperty("used")
    private Map<Integer, String> used;

    @CreatedDate
    @JsonProperty("created_at")
    private String created_at;

    @LastModifiedDate
    @JsonProperty("updated_at")
    private String updated_at;

    public NetworkSegmentRangeEntity() {}

    public NetworkSegmentRangeEntity(String projectId, String id, String name, String description, String networkType) {
        super(projectId, id, name, description);
        this.networkType = networkType;
    }

    public NetworkSegmentRangeEntity(String projectId, String id, String name, String description, boolean isDefault, Integer minimum, Integer maximum, String tags, String networkType, String physicalNetwork, Boolean shared, Integer revisionNumber, List<Integer> available, Map<Integer, String> used, String created_at, String updated_at) {
        super(projectId, id, name, description);
        this.isDefault = isDefault;
        this.minimum = minimum;
        this.maximum = maximum;
        this.tags = tags;
        this.networkType = networkType;
        this.physicalNetwork = physicalNetwork;
        this.shared = shared;
        this.revisionNumber = revisionNumber;
        this.available = available;
        this.used = used;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }
}
