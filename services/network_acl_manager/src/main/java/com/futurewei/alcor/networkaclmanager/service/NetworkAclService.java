package com.futurewei.alcor.networkaclmanager.service;

import com.futurewei.alcor.web.entity.networkacl.NetworkAclEntity;

import java.util.List;

public interface NetworkAclService {
    NetworkAclEntity createNetworkAcl(String projectId, NetworkAclEntity networkAclEntity) throws Exception;

    List<NetworkAclEntity> createNetworkAclBulk(String projectId, List<NetworkAclEntity> networkAclEntities) throws Exception;

    NetworkAclEntity updateNetworkAcl(String projectId, String networkAclId, NetworkAclEntity networkAclEntity) throws Exception;

    List<NetworkAclEntity> updateNetworkAclBulk(String projectId, List<NetworkAclEntity> networkAclEntities) throws Exception;

    void deleteNetworkAcl(String networkAclId) throws Exception;

    NetworkAclEntity getNetworkAcl(String networkAclId) throws Exception;

    List<NetworkAclEntity> listNetworkAcl() throws Exception;
}
