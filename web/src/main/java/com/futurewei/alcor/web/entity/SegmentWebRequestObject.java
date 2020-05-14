package com.futurewei.alcor.web.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import lombok.Data;

@Data
public class SegmentWebRequestObject extends CustomerResource {

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

    @JsonProperty("fields")
    private String fields;

    @JsonProperty("sort_dir")
    private String sortDir;

    @JsonProperty("sort_key")
    private String sortKey;

    public SegmentWebRequestObject () {}

    public SegmentWebRequestObject(String projectId, String id, String name, String description, String vpcId, String physicalNetwork, String networkType, Integer segmentationId, Integer revisionNumber, String fields, String sortDir, String sortKey) {
        super(projectId, id, name, description);
        this.vpcId = vpcId;
        this.physicalNetwork = physicalNetwork;
        this.networkType = networkType;
        this.segmentationId = segmentationId;
        this.revisionNumber = revisionNumber;
        this.fields = fields;
        this.sortDir = sortDir;
        this.sortKey = sortKey;
    }
}
