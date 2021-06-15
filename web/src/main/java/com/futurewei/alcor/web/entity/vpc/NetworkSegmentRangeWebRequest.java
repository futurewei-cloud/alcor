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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import lombok.Data;

@Data
public class NetworkSegmentRangeWebRequest extends CustomerResource {

    @JsonProperty("minimum")
    private Integer minimum;

    @JsonProperty("maximum")
    private Integer maximum;

    @JsonProperty("tags")
    private String tags;

    @JsonProperty("tags_any")
    private String tagsAny;

    @JsonProperty("not_tags")
    private String notTags;

    @JsonProperty("not_tags_any")
    private String notTagsAny;

    @JsonProperty("network_type")
    private String networkType;

    @JsonProperty("physical_network")
    private String physicalNetwork;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("sort_dir")
    private String sortDir;

    @JsonProperty("sort_key")
    private String sortKey;

    @JsonProperty("fields")
    private String fields;

    @JsonProperty("shared")
    private Boolean shared;

    public NetworkSegmentRangeWebRequest() {}

    public NetworkSegmentRangeWebRequest(String projectId, String id, String name, String description, Integer minimum, Integer maximum, String tags, String tagsAny, String notTags, String notTagsAny, String networkType, String physicalNetwork, String tenantId, String sortDir, String sortKey, String fields, Boolean shared) {
        super(projectId, id, name, description);
        this.minimum = minimum;
        this.maximum = maximum;
        this.tags = tags;
        this.tagsAny = tagsAny;
        this.notTags = notTags;
        this.notTagsAny = notTagsAny;
        this.networkType = networkType;
        this.physicalNetwork = physicalNetwork;
        this.tenantId = tenantId;
        this.sortDir = sortDir;
        this.sortKey = sortKey;
        this.fields = fields;
        this.shared = shared;
    }
}
