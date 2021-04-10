/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.web.entity.vpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import lombok.Data;

@Data
public class SegmentWebRequest extends CustomerResource {

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

    public SegmentWebRequest() {}

    public SegmentWebRequest(String projectId, String id, String name, String description, String vpcId, String physicalNetwork, String networkType, Integer segmentationId, Integer revisionNumber, String fields, String sortDir, String sortKey) {
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
