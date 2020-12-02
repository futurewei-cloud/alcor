package com.futurewei.alcor.dataplane.cache;

import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;
import com.futurewei.alcor.web.entity.subnet.InternalSubnetPorts;

public interface LocalCache {
    void addSubnetPorts(NetworkConfiguration networkConfig) throws Exception;
    void updateSubnetPorts(NetworkConfiguration networkConfig) throws Exception;
    void deleteSubnetPorts(NetworkConfiguration networkConfig);
    InternalSubnetPorts getSubnetPorts(String subnetId) throws Exception;
}
