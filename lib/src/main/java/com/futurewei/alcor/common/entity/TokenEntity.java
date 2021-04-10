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
