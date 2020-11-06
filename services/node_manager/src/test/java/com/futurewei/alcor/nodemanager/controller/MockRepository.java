package com.futurewei.alcor.nodemanager.controller;

import com.futurewei.alcor.nodemanager.config.UnitTestConfig;
import com.futurewei.alcor.nodemanager.dao.NodeRepository;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashMap;
import java.util.Map;

public class MockRepository {
    @MockBean
    private NodeRepository nodeRepository;

    @BeforeEach
    protected void mockRepositoryOperations() throws Exception {

        NodeInfo nodeInfo = new NodeInfo(UnitTestConfig.nodeId,
                UnitTestConfig.nodeName,
                UnitTestConfig.nodeLocalIp,
                UnitTestConfig.nodeMacAddress,
                UnitTestConfig.nodeVeth,
                UnitTestConfig.nodeGRPCServerPort,
                UnitTestConfig.hostDvrMacAddress);

        NodeInfo node2Info = new NodeInfo(UnitTestConfig.node2Id,
                UnitTestConfig.node2Name,
                UnitTestConfig.node2LocalIp,
                UnitTestConfig.node2MacAddress,
                UnitTestConfig.node2Veth,
                UnitTestConfig.node2GRPCServerPort,
                UnitTestConfig.host2DvrMacAddress);

        Map<String, NodeInfo> nodeInfos = new HashMap<>();
        nodeInfos.put(nodeInfo.getId(), nodeInfo);
        nodeInfos.put(node2Info.getId(), node2Info);

        Mockito.when(nodeRepository.findItem(UnitTestConfig.nodeId)).thenReturn(nodeInfo);

        Mockito.when(nodeRepository.findItem(UnitTestConfig.node2Id)).thenReturn(node2Info);

        Mockito.when(nodeRepository.findAllItems()).thenReturn(nodeInfos);

//        Mockito.when(nodeRepository.addItem(nodeInfo));
    }

    public static NodeInfoJson buildNodeInfoJson(String nodeId, String ipAddress, String portMacAddr, String hostDvrMacAddr) {
        NodeInfo nodeInfo = new NodeInfo(nodeId, UnitTestConfig.nodeName,
                ipAddress, portMacAddr, UnitTestConfig.nodeVeth,
                UnitTestConfig.nodeGRPCServerPort, hostDvrMacAddr);

        return new NodeInfoJson(nodeInfo);
    }

}
