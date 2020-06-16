package com.futurewei.alcor.networkaclmanager.service;

import com.futurewei.alcor.web.entity.networkacl.NetworkAclEntity;
import com.futurewei.alcor.web.entity.networkacl.NetworkAclWebJson;

import java.util.List;

public interface NetworkAclService {
    NetworkAclEntity createNetworkAcl(NetworkAclEntity networkAclEntity) throws Exception;

    //NetworkAclBulkJson createNetworkAclBulk(NetworkAclWebBulkJson networkAclWebBulkJson) throws Exception;

    NetworkAclEntity updateNetworkAcl(String networkAclId, NetworkAclEntity networkAclEntity) throws Exception;

    //NetworkAclBulkJson updateNetworkAclBulk(NetworkAclWebBulkJson networkAclWebBulkJson) throws Exception;

    void deleteNetworkAcl(String networkAclId) throws Exception;

    NetworkAclEntity getNetworkAcl(String networkAclId) throws Exception;

    List<NetworkAclEntity> listNetworkAcl() throws Exception;
}
