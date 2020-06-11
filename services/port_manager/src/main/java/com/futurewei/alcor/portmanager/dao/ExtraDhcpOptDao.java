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

import com.futurewei.alcor.web.entity.port.ExtraDhcpOpt;
import com.futurewei.alcor.web.entity.port.FixedIp;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExtraDhcpOptDao {

    @Select("select * from t_extra_dhcp_opt where port_id=#{portId}")
    List<FixedIp> findExtraDhcpOptsByPortId(String portId);

    @Insert("insert into t_extra_dhcp_opt (ip_version, opt_name, opt_value, port_id) values " +
            "(#{ipVersion}, #{optName}, #{optValue}, #{portId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    List<FixedIp> insertExtraDhcpOpt(String portId, ExtraDhcpOpt extraDhcpOpt);

    @Insert({
            "<script>",
            "insert into t_extra_dhcp_opt (ip_version, opt_name, opt_value, port_id) values" +
                    "<foreach collection='extraDhcpOpts' item='item' index='index' separator=','>",
            "(#{item.ipVersion}, #{item.optName}, #{item.optValue}, #{portId})",
            "</foreach>",
            "</script>"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertExtraDhcpOpts(String portId, @Param(value="extraDhcpOpts") List<ExtraDhcpOpt> extraDhcpOpts);
}
