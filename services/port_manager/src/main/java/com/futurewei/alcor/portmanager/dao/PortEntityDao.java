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

import com.futurewei.alcor.web.entity.port.PortEntity;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PortEntityDao {

    @Select("select * from t_port where id=#{id}")
    @Results({@Result(property = "id", column = "ID"),
            @Result(property = "fixedIps", column = "id", many = @Many(select = "com.futurewei.alcor.portmanager.dao.FixedIpDao.findFixedIpsByPortId")),
            @Result(property = "allowedAddressPairs", column = "id", many = @Many(select = "com.futurewei.alcor.portmanager.dao.AllowAddressPairDao.findAllowAddressPairsByPortId")),
            @Result(property = "extraDhcpOpts", column = "id", many = @Many(select = "com.futurewei.alcor.portmanager.dao.ExtraDhcpOptDao.findExtraDhcpOptsByPortId")),
            @Result(property = "securityGroups", column = "id", many = @Many(select = "com.futurewei.alcor.portmanager.dao.PortSecurityGroupDao.findPortSecurityGroupsByPortId")),
            @Result(property = "dnsAssignment", column = "id", many = @Many(select = "com.futurewei.alcor.portmanager.dao.DnsRecordDao.findDnsRecordsByPortId")),
            @Result(property = "tags", column = "id", many = @Many(select = "com.futurewei.alcor.portmanager.dao.PortTagDao.findPortTagsByPortId"))})
    PortEntity findPortById(String id);

    @Select("select * from t_port where name=#{name}")
    @Results({@Result(property = "id", column = "ID"),
            @Result(property = "fixedIps", column = "id", many = @Many(select = "com.futurewei.alcor.portmanager.dao.FixedIpDao.findFixedIpsByPortId")),
            @Result(property = "allowedAddressPairs", column = "id", many = @Many(select = "com.futurewei.alcor.portmanager.dao.AllowAddressPairDao.findAllowAddressPairsByPortId")),
            @Result(property = "extraDhcpOpts", column = "id", many = @Many(select = "com.futurewei.alcor.portmanager.dao.ExtraDhcpOptDao.findExtraDhcpOptsByPortId")),
            @Result(property = "securityGroups", column = "id", many = @Many(select = "com.futurewei.alcor.portmanager.dao.PortSecurityGroupDao.findPortSecurityGroupsByPortId")),
            @Result(property = "dnsAssignment", column = "id", many = @Many(select = "com.futurewei.alcor.portmanager.dao.DnsRecordDao.findDnsRecordsByPortId")),
            @Result(property = "tags", column = "id", many = @Many(select = "com.futurewei.alcor.portmanager.dao.PortTagDao.findPortTagsByPortId"))})
    PortEntity findPortByName(String name);

    @Select("select * from t_port")
    @Results({@Result(property = "id", column = "ID"),
            @Result(property = "fixedIps", column = "id", many = @Many(select = "com.futurewei.alcor.portmanager.dao.FixedIpDao.findFixedIpsByPortId")),
            @Result(property = "allowedAddressPairs", column = "id", many = @Many(select = "com.futurewei.alcor.portmanager.dao.AllowAddressPairDao.findAllowAddressPairsByPortId")),
            @Result(property = "extraDhcpOpts", column = "id", many = @Many(select = "com.futurewei.alcor.portmanager.dao.ExtraDhcpOptDao.findExtraDhcpOptsByPortId")),
            @Result(property = "securityGroups", column = "id", many = @Many(select = "com.futurewei.alcor.portmanager.dao.PortSecurityGroupDao.findPortSecurityGroupsByPortId")),
            @Result(property = "dnsAssignment", column = "id", many = @Many(select = "com.futurewei.alcor.portmanager.dao.DnsRecordDao.findDnsRecordsByPortId")),
            @Result(property = "tags", column = "id", many = @Many(select = "com.futurewei.alcor.portmanager.dao.PortTagDao.findPortTagsByPortId"))})
    List<PortEntity> listPort();

    @Insert("insert into t_port (id, name, project_id, description, vpc_id, tenant_id, admin_state_up," +
            "mac_address, veth_name, fast_path, device_id, device_owner, status, binding_host_id, binding_profile," +
            "binding_vif_details, binding_vif_type, binding_vnic_type, network_ns, dns_name, dns_domain, create_at," +
            "update_at, ip_allocation, port_security_enabled, qos_network_policy_id, qos_policy_id, revision_number," +
            "resource_request, uplink_status_propagation, mac_learning_enabled) values(#{id}, #{name}, #{projectId}," +
            "#{description}, #{vpcId}, #{tenantId}, #{adminStateUp}, #{macAddress}, #{vethName}, #{fastPath}," +
            "#{deviceId}, #{deviceOwner}, #{status}, #{bindingHostId}, #{bindingProfile}, #{bindingVifDetails}," +
            "#{bindingVifType}, #{bindingVnicType}, #{networkNamespace}, #{dnsName}, #{dnsDomain}, #{createAt, jdbcType=TIMESTAMP}, " +
            "#{updateAt, jdbcType=TIMESTAMP}, #{ipAllocation}, #{portSecurityEnabled}, #{qosNetworkPolicyId}, " +
            "#{qosPolicyId}, #{revisionNumber}, #{resourceRequest}, #{uplinkStatusPropagation}, #{macLearningEnabled})")
    void insertPort(PortEntity portEntity);

    @Insert({
            "<script>",
            "insert into t_port (id, name, project_id, description, vpc_id, tenant_id, admin_state_up," +
                    "mac_address, veth_name, fast_path, device_id, device_owner, status, binding_host_id, binding_profile," +
                    "binding_vif_details, binding_vif_type, binding_vnic_type, network_ns, dns_name, dns_domain, create_at," +
                    "update_at, ip_allocation, port_security_enabled, qos_network_policy_id, qos_policy_id, revision_number," +
                    "resource_request, uplink_status_propagation, mac_learning_enabled) values" +
                    "<foreach collection='portEntities' item='item' index='index' separator=','>",
                    "(#{item.id}, #{item.name}, #{item.projectId}, #{item.description}, #{item.vpcId}, " +
                    "#{item.tenantId}, #{item.adminStateUp}, #{item.macAddress}, #{item.vethName}, #{item.fastPath}," +
                    "#{item.deviceId}, #{item.deviceOwner}, #{item.status}, #{item.bindingHostId}, #{item.bindingProfile}, " +
                    "#{item.bindingVifDetails},#{item.bindingVifType}, #{item.bindingVnicType}, #{item.networkNamespace}, " +
                    "#{item.dnsName}, #{item.dnsDomain}, #{item.createAt, jdbcType=TIMESTAMP}, #{item.updateAt, jdbcType=TIMESTAMP}, " +
                    "#{item.ipAllocation}, #{item.portSecurityEnabled}, #{item.qosNetworkPolicyId}, " +
                    "#{item.qosPolicyId}, #{item.revisionNumber}, #{item.resourceRequest}, #{item.uplinkStatusPropagation}, #{item.macLearningEnabled})",
            "</foreach>",
            "</script>"
            })
    void insertPorts(@Param(value="portEntities") List<PortEntity> portEntities);

    @Update("update t_port set name=#{name}, project_id=#{projectId}, description=#{description}, vpc_id=#{vpcId}," +
            "tenant_id=#{tenantId}, admin_state_up=#{adminStateUp}, mac_address=#{macAddress}, veth_name=#{vethName}," +
            "fast_path=#{fastPath}, device_id=#{deviceId}, device_owner=#{deviceOwner}, status=#{status}, " +
            "binding_host_id=#{bindingHostId}, binding_profile=#{bindingProfile}, binding_vif_details=#{bindingVifDetails}," +
            "binding_vif_type=#{bindingVifType}, binding_vnic_type=#{bindingVnicType}, network_ns=#{networkNamespace}," +
            "dns_name=#{dnsName}, dns_domain=#{dnsName}, create_at=#{createAt, jdbcType=TIMESTAMP}, " +
            "update_at=#{updateAt, jdbcType=TIMESTAMP}, ip_allocation=#{ipAllocation}, port_security_enabled=#{portSecurityEnabled}," +
            "qos_network_policy_id=#{qosNetworkPolicyId}, qos_policy_id=#{qosPolicyId}, revision_number=#{revisionNumber}," +
            "resource_request=#{resourceRequest}, uplink_status_propagation=#{uplinkStatusPropagation}, " +
            "mac_learning_enabled=#{macLearningEnabled}")
    void updatePort(PortEntity portEntity);

    @Delete("delete from t_port where id=#{id}")
    void deletePort(String id);
}
