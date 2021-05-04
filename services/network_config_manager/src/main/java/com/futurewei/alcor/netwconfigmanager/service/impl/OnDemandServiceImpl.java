/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.netwconfigmanager.service.impl;

import com.futurewei.alcor.netwconfigmanager.cache.HostResourceMetadataCache;
import com.futurewei.alcor.netwconfigmanager.cache.ResourceStateCache;
import com.futurewei.alcor.netwconfigmanager.cache.VpcResourceCache;
import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.netwconfigmanager.entity.ResourceMeta;
import com.futurewei.alcor.netwconfigmanager.entity.VpcResourceMeta;
import com.futurewei.alcor.netwconfigmanager.service.OnDemandService;
import com.futurewei.alcor.schema.*;
import io.grpc.Grpc;
import io.grpc.ServerCall;
import io.grpc.ServerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static io.grpc.Grpc.TRANSPORT_ATTR_REMOTE_ADDR;

@Service
public class OnDemandServiceImpl implements OnDemandService {

    @Autowired
    private HostResourceMetadataCache hostResourceMetadataCache;

    @Autowired
    private ResourceStateCache resourceStateCache;

    @Autowired
    private VpcResourceCache vpcResourceCache;

    @Override
    public HostGoalState retrieveGoalState(Goalstateprovisioner.HostRequest.ResourceStateRequest resourceStateRequest) throws Exception {
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
        String vni = String.valueOf(resourceStateRequest.getTunnelId());
        String sourceIp = resourceStateRequest.getSourceIp();
        String destinationIp = resourceStateRequest.getDestinationIp();
        String hostIp = "";

        ResourceMeta resourceMetadata = retrieveResourceMeta(vni, sourceIp);
        //TODO: handle resource metadata not found
        if (resourceMetadata == null) {
            //...
        }

        List<ResourceMeta> resourceMetas = new ArrayList<>() {
            {
                add(resourceMetadata);
            }
        };
        Goalstate.GoalStateV2 goalState = retrieveResourceState(resourceMetas);
        HostGoalState hostGoalState = new HostGoalState(hostIp, goalState);

        return hostGoalState;
    }

    @Override
    public ResourceMeta retrieveResourceMeta(String vni, String privateIp) throws Exception {

        VpcResourceMeta curResourceMeta = vpcResourceCache.getResourceMeta(vni);
        if (curResourceMeta == null)
            return null;

        return curResourceMeta.getResourceMetas(privateIp);
    }

    @Override
    public Goalstate.GoalStateV2 retrieveResourceState(List<ResourceMeta> resourceMetas) throws Exception {

        if (resourceMetas == null) {
            return null;
        }

        // TODO: This triggers quite a few db read access. Need to evaluate performance or rewrite with bulk access
        Goalstate.GoalStateV2.Builder builder = Goalstate.GoalStateV2.newBuilder();
        for (ResourceMeta resource : resourceMetas) {
            String ownerId = resource.getOwnerId();
            for (String vpcId : resource.getVpcIds()) {
                builder.putVpcStates(vpcId, (Vpc.VpcState) resourceStateCache.getResourceState(vpcId));
            }
            for (String subnetId : resource.getSubnetIds()) {
                builder.putSubnetStates(subnetId, (Subnet.SubnetState) resourceStateCache.getResourceState(subnetId));
            }
            for (String portId : resource.getPortIds()) {
                builder.putPortStates(portId, (Port.PortState) resourceStateCache.getResourceState(portId));
            }
            for (String neighborId : resource.getNeighborIdMap().values()) {
                builder.putNeighborStates(neighborId, (Neighbor.NeighborState) resourceStateCache.getResourceState(neighborId));
            }
            for (String dhcpId : resource.getDhcpIds()) {
                builder.putDhcpStates(dhcpId, (DHCP.DHCPState) resourceStateCache.getResourceState(dhcpId));
            }
            for (String routerId : resource.getRouterIds()) {
                builder.putRouterStates(routerId, (Router.RouterState) resourceStateCache.getResourceState(routerId));
            }
        }

        return builder.build();
    }
}
