/*
 *
 * Copyright 2019 The Alcor Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * /
 */

package com.futurewei.alcor.common.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FieldRbacRule {

    private String name;

    private String description;

    @JsonProperty("rule_type")
    private String ruleType;

    private List<String> roles;

    public FieldRbacRule() {}

    public FieldRbacRule(String name, String ruleType) {
        this(name, null, ruleType);
    }

    public FieldRbacRule(String name, String description, String ruleType) {
        this(name, description, ruleType, null);
    }

    public FieldRbacRule(String name, String description, String ruleType, List<String> roles) {
        this.name = name;
        this.description = description;
        this.ruleType = ruleType;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
