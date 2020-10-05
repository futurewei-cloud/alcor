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

public class ActionRbacRule {

    private String name;

    @JsonProperty("method_type")
    private String methodType;

    private String description;

    @JsonProperty("rule_type")
    private String ruleType;

    private List<String> roles;

    @JsonProperty("field_rbac_rules")
    private List<FieldRbacRule> fieldRbacRules;

    public ActionRbacRule() {}

    public ActionRbacRule(String name, String methodType, String ruleType) {
        this(name, methodType, null, ruleType, null, null);
    }

    public ActionRbacRule(String name, String methodType, String description, String ruleType) {
        this(name, methodType, description, ruleType, null, null);
    }

    public ActionRbacRule(String name, String methodType, String description, String ruleType,
                          List<String> roles) {
        this(name, methodType, description, ruleType, roles, null);
    }

    public ActionRbacRule(String name, String methodType, String description, String ruleType,
                          List<String> roles, List<FieldRbacRule> fieldRbacRules) {
        this.name = name;
        this.methodType = methodType;
        this.description = description;
        this.ruleType = ruleType;
        this.roles = roles;
        this.fieldRbacRules = fieldRbacRules;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
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

    public List<FieldRbacRule> getFieldRbacRules() {
        return fieldRbacRules;
    }

    public void setFieldRbacRules(List<FieldRbacRule> fieldRbacRules) {
        this.fieldRbacRules = fieldRbacRules;
    }
}
