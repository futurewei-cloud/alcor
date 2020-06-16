package com.futurewei.alcor.networkaclmanager.service;

import com.futurewei.alcor.web.entity.networkacl.NetworkAclRuleEntity;
import com.futurewei.alcor.web.entity.networkacl.NetworkAclRuleWebJson;
import java.util.List;

public interface NetworkAclRuleService {
    NetworkAclRuleEntity createNetworkAclRule(NetworkAclRuleEntity networkAclRuleEntity) throws Exception;

    //NetworkAclRuleWebBulkJson createNetworkAclRuleBulk(String projectId, NetworkAclRuleWebBulkJson networkAclRuleWebBulkJson) throws Exception;

    NetworkAclRuleEntity updateNetworkAclRule(String networkAclRuleId, NetworkAclRuleEntity networkAclRuleEntity) throws Exception;

    //NetworkAclRuleWebBulkJson updateNetworkAclRuleBulk(String projectId, NetworkAclRuleWebBulkJson networkAclRuleWebBulkJson) throws Exception;

    void deleteNetworkAclRule(String networkAclRuleId) throws Exception;

    NetworkAclRuleEntity getNetworkAclRule(String networkAclRuleId) throws Exception;

    List<NetworkAclRuleEntity> listNetworkAclRule() throws Exception;
}
