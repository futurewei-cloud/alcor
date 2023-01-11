package com.futurewei.alcor.dataplane.service.impl;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.dataplane.cache.PortBindingSecurityGroupRepository;
import com.futurewei.alcor.dataplane.cache.SecurityGroupRepository;
import com.futurewei.alcor.dataplane.client.DataPlaneClient;
import com.futurewei.alcor.dataplane.entity.MulticastGoalStateV2;
import com.futurewei.alcor.dataplane.entity.UnicastGoalStateV2;
import com.futurewei.alcor.dataplane.exception.SecurityGroupDirectionInvalid;
import com.futurewei.alcor.dataplane.exception.SecurityGroupEtherTypeInvalid;
import com.futurewei.alcor.dataplane.exception.SecurityGroupProtocolInvalid;
import com.futurewei.alcor.dataplane.service.SecurityGroupService;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.SecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.PortBindingSecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRuleBulkJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRuleJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SecurityGroupServiceImpl implements SecurityGroupService {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityGroupServiceImpl.class);

    @Autowired
    private PortBindingSecurityGroupRepository portBindingSecurityGroupRepository;

    @Autowired
    private SecurityGroupRepository securityGroupRepository;

    @Autowired
    private DataPlaneClient<UnicastGoalStateV2, MulticastGoalStateV2> grpcDataPlaneClient;


    private SecurityGroup.SecurityGroupConfiguration.Direction transformDirection(String direction) throws Exception {
        if (StringUtils.isEmpty(direction)) {
            throw new SecurityGroupDirectionInvalid();
        }

        switch (direction) {
            case "ingress":
                return SecurityGroup.SecurityGroupConfiguration.Direction.INGRESS;
            case "egress":
                return SecurityGroup.SecurityGroupConfiguration.Direction.EGRESS;
        }

        throw new SecurityGroupDirectionInvalid();
    }

    private Common.EtherType transformEtherType(String etherType) throws Exception {
        if (StringUtils.isEmpty(etherType)) {
            return Common.EtherType.IPV4;
        }

        switch (etherType) {
            case "IPv4":
                return Common.EtherType.IPV4;
            case "IPv6":
                return Common.EtherType.IPV6;
        }

        throw new SecurityGroupEtherTypeInvalid();
    }

    private Common.Protocol transformProtocol(String protocol) throws Exception {
        if (StringUtils.isEmpty(protocol)) {
            throw new SecurityGroupProtocolInvalid();
        }

        switch (protocol) {
            case "tcp":
                return Common.Protocol.TCP;
            case "udp":
                return Common.Protocol.UDP;
            case "icmp":
                return Common.Protocol.ICMP;
            case "http":
                return Common.Protocol.HTTP;
        }

        throw new SecurityGroupProtocolInvalid();
    }

    public SecurityGroup.SecurityGroupConfiguration.SecurityGroupRule buildSecurityGroupGoalState(SecurityGroupRule securityGroupRule) throws Exception {
        SecurityGroup.SecurityGroupConfiguration.SecurityGroupRule.Builder securityGroupRuleBuilder = SecurityGroup.SecurityGroupConfiguration.SecurityGroupRule.newBuilder();

        securityGroupRuleBuilder.setSecurityGroupId(securityGroupRule.getId());
        securityGroupRuleBuilder.setOperationType(Common.OperationType.CREATE);
        securityGroupRuleBuilder.setId(securityGroupRule.getId());
        securityGroupRuleBuilder.setDirection(transformDirection(securityGroupRule.getDirection()));
        securityGroupRuleBuilder.setEthertype(transformEtherType(securityGroupRule.getEtherType()));

        if (securityGroupRule.getProtocol() != null) {
            securityGroupRuleBuilder.setProtocol(transformProtocol(securityGroupRule.getProtocol()));
        }

        if (securityGroupRule.getPortRangeMin() != null) {
            securityGroupRuleBuilder.setPortRangeMin(securityGroupRule.getPortRangeMin());
        }

        if (securityGroupRule.getPortRangeMax() != null) {
            securityGroupRuleBuilder.setPortRangeMax(securityGroupRule.getPortRangeMax());
        }

        if (securityGroupRule.getRemoteIpPrefix() != null) {
            securityGroupRuleBuilder.setRemoteIpPrefix(securityGroupRule.getRemoteIpPrefix());
        }

        if (securityGroupRule.getRemoteGroupId() != null) {
            securityGroupRuleBuilder.setRemoteGroupId(securityGroupRule.getRemoteGroupId());
        }
        return securityGroupRuleBuilder.build();
    }


    @Override
    public void updateSecurityGroupRule(SecurityGroupRuleJson securityGroupRuleJson) throws Exception {
        SecurityGroup.SecurityGroupState.Builder securityGroupStateBuilder = SecurityGroup.SecurityGroupState.newBuilder();
        SecurityGroup.SecurityGroupConfiguration.Builder securityGroupConfigurationBuilder = SecurityGroup.SecurityGroupConfiguration.newBuilder();
        List<SecurityGroup.SecurityGroupConfiguration.SecurityGroupRule> securityGroupRules = new ArrayList<>();
        securityGroupRules.add(buildSecurityGroupGoalState(securityGroupRuleJson.getSecurityGroupRule()));
        securityGroupConfigurationBuilder.addAllSecurityGroupRules(securityGroupRules);
        securityGroupStateBuilder.setConfiguration(securityGroupConfigurationBuilder);

        grpcDataPlaneClient.sendGoalStates(securityGroupStateBuilder.build());
        securityGroupRepository.addSecurityGroupRule(securityGroupRuleJson);

    }

    @Override
    public void updateSecurityGroupRules(SecurityGroupRuleBulkJson securityGroupRuleBulkJson) throws Exception {
        SecurityGroup.SecurityGroupState.Builder securityGroupStateBuilder = SecurityGroup.SecurityGroupState.newBuilder();
        SecurityGroup.SecurityGroupConfiguration.Builder securityGroupConfigurationBuilder = SecurityGroup.SecurityGroupConfiguration.newBuilder();
        List<SecurityGroup.SecurityGroupConfiguration.SecurityGroupRule> securityGroupRules = new ArrayList<>();
        for (var securityGroupRule : securityGroupRuleBulkJson.getSecurityGroupRules()) {
            securityGroupRules.add(buildSecurityGroupGoalState(securityGroupRule));
        }
        securityGroupConfigurationBuilder.addAllSecurityGroupRules(securityGroupRules);
        securityGroupStateBuilder.setConfiguration(securityGroupConfigurationBuilder);

        grpcDataPlaneClient.sendGoalStates(securityGroupStateBuilder.build());
        securityGroupRepository.addSecurityGroupRules(securityGroupRuleBulkJson);
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
    public Collection<PortBindingSecurityGroup> getPortBindingSecurityGroupByPortId(String portId) throws CacheException {
        return portBindingSecurityGroupRepository.getPortBindingSecurityGroupByPortId(portId);
    }
}
