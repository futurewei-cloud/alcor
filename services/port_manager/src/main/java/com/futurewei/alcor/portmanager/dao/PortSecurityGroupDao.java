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
package com.futurewei.alcor.portmanager.dao;

import com.futurewei.alcor.web.entity.port.FixedIp;
import com.futurewei.alcor.web.entity.port.PortSecurityGroup;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PortSecurityGroupDao {
    @Select("select * from t_port_security_group where port_id=#{portId}")
    List<FixedIp> findPortSecurityGroupsByPortId(String portId);

    @Insert("insert into t_port_security_group (security_group_id, port_id) values " +
            "(#{securityGroupId}, #{portId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    List<FixedIp> insertPortSecurityGroup(String portId, PortSecurityGroup portSecurityGroup);

    @Insert({
            "<script>",
            "insert into t_port_security_group (security_group_id, port_id) values" +
                    "<foreach collection='portSecurityGroups' item='item' index='index' separator=','>",
            "(#{item}, #{portId})",
            "</foreach>",
            "</script>"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertPortSecurityGroups(String portId, @Param(value="portSecurityGroups") List<String> portSecurityGroups);
}
