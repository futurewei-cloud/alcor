package com.futurewei.alcor.networkaclmanager.service;

import com.futurewei.alcor.web.entity.networkacl.NetworkAclRuleEntity;
import java.util.List;

public interface NetworkAclRuleService {
    NetworkAclRuleEntity createNetworkAclRule(NetworkAclRuleEntity networkAclRuleEntity) throws Exception;

    List<NetworkAclRuleEntity> createNetworkAclRuleBulk(List<NetworkAclRuleEntity> networkAclRuleEntities) throws Exception;

    NetworkAclRuleEntity updateNetworkAclRule(String networkAclRuleId, NetworkAclRuleEntity networkAclRuleEntity) throws Exception;

    List<NetworkAclRuleEntity> updateNetworkAclRuleBulk(List<NetworkAclRuleEntity> networkAclRuleEntities) throws Exception;

    void deleteNetworkAclRule(String networkAclRuleId) throws Exception;

    NetworkAclRuleEntity getNetworkAclRule(String networkAclRuleId) throws Exception;

    List<NetworkAclRuleEntity> listNetworkAclRule() throws Exception;
}
