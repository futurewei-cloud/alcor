/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/

package com.futurewei.alcor.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.futurewei.alcor.common.utils.DateUtil;
import lombok.Data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TokenEntity implements Serializable {

    String token;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.KEYSTONE_TOKEN_DATE_PATTERN)
    Date expireAt;

    String user;
    String userId;
    String domainId;
    String domainName;
    String projectName;
    String projectId;
    String projectDomain;
    boolean invalid;
    List<String> roles;

    // a default construct for json write
    public TokenEntity(){}

    public TokenEntity(String token, boolean invalid){
        this.invalid = invalid;
        this.token = token;
    }

    @JsonIgnore
    public boolean isExpired(){
        return invalid || (expireAt != null && expireAt.before(new Date()));
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectDomain() {
        return projectDomain;
    }

    public void setProjectDomain(String projectDomain) {
        this.projectDomain = projectDomain;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "TokenEntity{" +
                "token='" + token + '\'' +
                ", expireAt=" + expireAt +
                ", user='" + user + '\'' +
                ", userId='" + userId + '\'' +
                ", domainId='" + domainId + '\'' +
                ", domainName='" + domainName + '\'' +
                ", projectName='" + projectName + '\'' +
                ", projectId='" + projectId + '\'' +
                ", projectDomain='" + projectDomain + '\'' +
                ", invalid=" + invalid +
                ", roles=" + roles +
                '}';
    }

    public String toJson() {
        String expireAtStr = expireAt == null ? "":DateUtil.getKeystoneDateFormat().format(expireAt);
        return "{" +
                "\"token\":\"" + token + "\"" +
                ", \"expireAt\":\"" + expireAtStr + "\"" +
                ", \"user\":\"" + user + "\"" +
                ", \"userId\":\"" + userId + "\"" +
                ", \"domainId\":\"" + domainId + "\"" +
                ", \"domainName\":\"" + domainName + "\"" +
                ", \"projectName\":\"" + projectName + "\"" +
                ", \"projectId\":\"" + projectId + "\"" +
                ", \"projectDomain\":\"" + projectDomain + "\"" +
                ", \"roles\":" + listJsonStr(roles) +
                "}";
    }

    private String listJsonStr(List<String> list){
        if(list == null || list.isEmpty()){
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int size = list.size();
        for(int i=0; i < size - 1; i++){
            sb.append("\"").append(list.get(i)).append("\",");
        }
        sb.append("\"").append(list.get(size - 1)).append("\"");
        return sb.append("]").toString();
    }
}
