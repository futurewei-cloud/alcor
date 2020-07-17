package com.futurewei.alcor.web.entity.securitygroup;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SecurityGroupsJson {

    @JsonProperty("security_groups")
    private ArrayList<SecurityGroup> securityGroups;

    public SecurityGroupsJson() {

    }

    public SecurityGroupsJson(List<SecurityGroup> securityGroups) {
        this.securityGroups = new ArrayList<>(securityGroups);
    }
}
