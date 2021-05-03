package com.futurewei.alcor.netwconfigmanager.service.impl;

import com.futurewei.alcor.netwconfigmanager.cache.HostResourceMetadataCache;
import com.futurewei.alcor.netwconfigmanager.cache.ResourceStateCache;
import com.futurewei.alcor.netwconfigmanager.cache.VpcResourceCache;
import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.netwconfigmanager.entity.ResourceMeta;
import com.futurewei.alcor.netwconfigmanager.service.OnDemandService;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class OnDemandServiceImpl implements OnDemandService {

    @Autowired
    private HostResourceMetadataCache hostResourceMetadataCache;

    @Autowired
    private ResourceStateCache resourceStateCache;

    @Autowired
    private VpcResourceCache vpcResourceCache;

    @Override
    public HostGoalState retrieveGoalState(Goalstateprovisioner.HostRequest.ResourceStateRequest resourceStateRequest) {
        /////////////////////////////////////////////////////////////////////////////////////////
        //                  On-Demand Algorithm
        // query cache M4 by VNI and source IP, and find all associated list of resource IDs (along with type)
        // Based on the resource type and id, query cache M3 to find its detailed state
        // 1. For resource type == NEIGHBOR, check if there exists resource.IP == request.destination_ip
        //                                  => yes, go to step 2 | no, reject
        // 2. If this port is SG enabled, go to step 3 | no, skip Step 3 and go to Step 4
        // 3. For resource type == SECURITY_GROUP, assuming that this packet must be outbound (as inbound packet has
        //    been handled by SG label), check if the 5-tuples (ip/port, destination ip/port, protocol) + ethertype
        //    comply with the outbound SG rules of source port
        // 3.1 query cache M3 based on associated SG IDs of the source port and retrieve existing SG detail
        // 3.2 for each SG and SG rule, check if outbound rule allows the 5-tuples + ethertype,
        //                             => yes, go to step 4 | no, go to Step 3.3
        // 3.3 (optional) if a rule includes a remote SG id, query cache M3 and retrieve detailed membership of
        //                remote SG, check whether destination id belongs to the remote SG
        //                             => yes, go to Step 4 | no, reject
        // 4. Bingo! this packet is allowed, collect port related resources (NEIGHBOR, SG etc. FULL GS) and send down
        //    to ACA by a separate gRPC client


        return null;
    }

    @Override
    public List<ResourceMeta> retrieveResourceMeta(String vni, String privateIp) {
        return null;
    }

    @Override
    public List<Object> retrieveResourceState(List<String> resourceIds) {
        return null;
    }
}
