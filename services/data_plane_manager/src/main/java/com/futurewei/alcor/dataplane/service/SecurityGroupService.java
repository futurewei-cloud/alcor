package com.futurewei.alcor.dataplane.service;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.web.entity.securitygroup.PortBindingSecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRuleBulkJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRuleJson;

import java.util.Collection;
import java.util.List;

public interface SecurityGroupService {
    void updateSecurityGroupRule(SecurityGroupRuleJson securityGroupRuleJson) throws Exception;

    void updateSecurityGroupRules(SecurityGroupRuleBulkJson securityGroupRuleBulkJson) throws Exception;

    void deleteSecurityGroupRules(List<String> securityGroupIds) throws Exception;

    List<SecurityGroupRule> getSecurityGroupRules(List<String> securityGroupRuleIds) throws CacheException;

    Collection<SecurityGroupRule> getSecurityGroupRules(String securityGroupId) throws CacheException;

    void addPortBindingSecurityGroup(List<PortBindingSecurityGroup> portBindingSecurityGroups) throws Exception;

    void deletePortBindingSecurityGroup (List<PortBindingSecurityGroup> portBindingSecurityGroups) throws Exception;

    Collection<PortBindingSecurityGroup> getPortBindingSecurityGroupBySecurityGroupId(String securityGroupId) throws CacheException;

    Collection<PortBindingSecurityGroup> getPortBindingSecurityGroupByPortId(String portId) throws CacheException;
}
