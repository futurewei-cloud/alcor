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
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FixedIpDao {

    @Select("select * from t_fixed_ip where port_id=#{portId}")
    List<FixedIp> findFixedIpsByPortId(String portId);

    @Insert("insert into t_fixed_ip (subnet_id, ip_address, port_id) values " +
            "(#{subnetId}, #{ipAddress}, #{portId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    List<FixedIp> insertFixedIp(String portId, FixedIp fixedIp);

    @Insert({
            "<script>",
            "insert into t_fixed_ip (subnet_id, ip_address, port_id) values" +
                    "<foreach collection='fixedIps' item='item' index='index' separator=','>",
            "(#{item.subnetId}, #{item.ipAddress}, #{portId})",
            "</foreach>",
            "</script>"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertFixedIps(String portId, @Param(value="fixedIps") List<FixedIp> fixedIps);
}
