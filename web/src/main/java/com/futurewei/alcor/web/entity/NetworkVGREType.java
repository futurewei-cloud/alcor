package com.futurewei.alcor.web.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Data
public class NetworkVGREType {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @CreatedDate
    @JsonProperty("created_at")
    private String created_at;

    @LastModifiedDate
    @JsonProperty("updated_at")
    private String updated_at;

    public NetworkVGREType() {
    }

}
