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

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class TokenEntity implements Serializable {

    String token;
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

    public TokenEntity(String token, boolean invalid){
        this.invalid = invalid;
        this.token = token;
    }

    public boolean isExpired(){
        return invalid || (expireAt != null && expireAt.before(new Date()));
    }

}
