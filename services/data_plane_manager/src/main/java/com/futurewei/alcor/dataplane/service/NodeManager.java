package com.futurewei.alcor.dataplane.service;

import com.futurewei.alcor.dataplane.entity.HostInfo;
import com.futurewei.alcor.dataplane.service.impl.NodeManagerImpl;

public interface NodeManager {

    public static int GRPC_SERVER_PORT = 50001;

    public static NodeManagerImpl nodeManager = null;

    public HostInfo getHostInfoById(String hostId) ;

    public HostInfo[] getRandomHosts(int count) ;

}
