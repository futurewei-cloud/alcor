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
