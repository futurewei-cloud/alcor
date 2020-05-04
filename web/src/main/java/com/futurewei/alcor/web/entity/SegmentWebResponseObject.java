package com.futurewei.alcor.web.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Data
public class SegmentWebResponseObject extends CustomerResource {

    @JsonProperty("network_id")
    private String vpcId;

    @JsonProperty("physical_network")
    private String physicalNetwork;

    @JsonProperty("network_type")
    private String networkType;

    @JsonProperty("segmentation_id")
    private Integer segmentationId;

    @JsonProperty("revision_number")
    private Integer revisionNumber;

    @CreatedDate
    @JsonProperty("created_at")
    private String created_at;

    @LastModifiedDate
    @JsonProperty("updated_at")
    private String updated_at;

    public SegmentWebResponseObject () {}

    public SegmentWebResponseObject(String projectId, String id, String name, String description, String vpcId) {
        super(projectId, id, name, description);
        this.vpcId = vpcId;
    }

    public SegmentWebResponseObject(String projectId, String id, String name, String description, String vpcId, String physicalNetwork, String networkType, Integer segmentationId, Integer revisionNumber, String created_at, String updated_at) {
        super(projectId, id, name, description);
        this.vpcId = vpcId;
        this.physicalNetwork = physicalNetwork;
        this.networkType = networkType;
        this.segmentationId = segmentationId;
        this.revisionNumber = revisionNumber;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }
}
