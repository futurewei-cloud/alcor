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

import com.futurewei.alcor.web.entity.port.DnsRecord;
import com.futurewei.alcor.web.entity.port.FixedIp;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DnsRecordDao {

    @Select("select * from t_dns_record where port_id=#{portId}")
    List<FixedIp> findDnsRecordsByPortId(String portId);

    @Insert("insert into t_dns_record (host_name, ip_address, fqdn, port_id) values " +
            "(#{hostName}, #{ipAddress}, #{fqdn}, #{portId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    List<FixedIp> insertDnsRecord(String portId, DnsRecord dnsRecord);

    @Insert({
            "<script>",
            "insert into t_dns_record (host_name, ip_address, fqdn, port_id) values" +
                    "<foreach collection='dnsRecords' item='item' index='index' separator=','>",
            "(#{item.hostName}, #{item.ipAddress}, #{item.fqdn}, #{portId})",
            "</foreach>",
            "</script>"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertDnsRecords(String portId, @Param(value="dnsRecords") List<DnsRecord> dnsRecords);
}
