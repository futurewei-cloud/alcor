/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.portmanager.request;

import com.futurewei.alcor.portmanager.entity.PortNeighbors;
import com.futurewei.alcor.portmanager.repo.PortRepository;

import java.util.ArrayList;
import java.util.List;

public class FetchPortNeighborRequest implements UpstreamRequest {
    private PortRepository portRepository;
    private List<String> vpcIds;
    private List<PortNeighbors> portNeighborsList;

    public FetchPortNeighborRequest(PortRepository portRepository, List<String> vpcIds) {
        this.portRepository = portRepository;
        this.vpcIds = vpcIds;
        this.portNeighborsList = new ArrayList<>();
    }

    public List<PortNeighbors> getPortNeighborsList() {
        return portNeighborsList;
    }

    @Override
    public void send() throws Exception {
        for (String vpcId: vpcIds) {
            portRepository.getPortNeighbors(vpcId);
        }
    }

    @Override
    public void rollback() throws Exception {

    }
}
