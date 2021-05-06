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
package com.futurewei.alcor.netwconfigmanager.service;

import com.futurewei.alcor.netwconfigmanager.entity.HostGoalState;
import com.futurewei.alcor.netwconfigmanager.entity.ResourceMeta;
import com.futurewei.alcor.netwconfigmanager.entity.VpcResourceMeta;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Goalstateprovisioner;

import java.util.List;

public interface OnDemandService {

    /**
     * Retrieve Resource Goal State according to the received resource state request.
     *
     * @param resourceStateRequest
     * @return HostGoalState
     * @throws Exception Various exceptions that may occur during the create process
     */
    HostGoalState retrieveGoalState(Goalstateprovisioner.HostRequest.ResourceStateRequest resourceStateRequest, String hostIpAddress) throws Exception;

    VpcResourceMeta retrieveResourceMeta(String vni, String privateIp) throws Exception;

    Goalstate.GoalStateV2 retrieveResourceState(List<ResourceMeta> resourceMetas) throws Exception;
}
