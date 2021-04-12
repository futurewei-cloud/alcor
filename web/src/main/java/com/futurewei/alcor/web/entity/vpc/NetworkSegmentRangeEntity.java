/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.web.entity.vpc;

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
