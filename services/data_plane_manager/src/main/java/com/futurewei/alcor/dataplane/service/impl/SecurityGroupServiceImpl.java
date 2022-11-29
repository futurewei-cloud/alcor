package com.futurewei.alcor.dataplane.service.impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.dataplane.cache.PortBindingSecurityGroupRepository;
import com.futurewei.alcor.dataplane.cache.SecurityGroupRepository;
import com.futurewei.alcor.dataplane.service.SecurityGroupService;
import com.futurewei.alcor.web.entity.securitygroup.PortBindingSecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SecurityGroupServiceImpl implements SecurityGroupService {
    private static final Logger LOG = LoggerFactory.getLogger(DpmServiceImpl.class);


    @Autowired
    private PortBindingSecurityGroupRepository portBindingSecurityGroupRepository;


    @Autowired
    private SecurityGroupRepository securityGroupRepository;


    @Override
    public void updateSecurityGroupRules(List<SecurityGroupRule> securityGroupRuleList) throws Exception {
        securityGroupRepository.addSecurityGroupRules(securityGroupRuleList);
    }

    @Override
    public void deleteSecurityGroupRules(List<String> securityGroupIds) throws Exception {
        securityGroupRepository.deleteSecurityGroupRules(securityGroupIds);
    }

    @Override
    public List<SecurityGroupRule> getSecurityGroupRules(List<String> securityGroupRuleIds) throws CacheException {
        return securityGroupRepository.getSecurityGroupRules(securityGroupRuleIds);
    }

    @Override
    public Collection<SecurityGroupRule> getSecurityGroupRules(String securityGroupId) throws CacheException {
        return securityGroupRepository.getSecurityGroupRules(securityGroupId);
    }

    @Override
    public void addPortBindingSecurityGroup(List<PortBindingSecurityGroup> portBindingSecurityGroups) throws Exception {
        portBindingSecurityGroupRepository.addPortBindingSecurityGroup(portBindingSecurityGroups);
    }

    @Override
    public void deletePortBindingSecurityGroup(List<PortBindingSecurityGroup> portBindingSecurityGroups) throws Exception {
        portBindingSecurityGroupRepository.deleteSecurityGroupBinding(portBindingSecurityGroups);
    }

    @Override
    public Collection<PortBindingSecurityGroup> getPortBindingSecurityGroupBySecurityGroupId(String securityGroupId) throws CacheException {
        return portBindingSecurityGroupRepository.getPortBindingSecurityGroupBySecurityGroupId(securityGroupId);
    }

    @Override
    public Collection<PortBindingSecurityGroup> getPortBindingSecurityGroupByPortId(String portId) {
        return null;
    }
}
