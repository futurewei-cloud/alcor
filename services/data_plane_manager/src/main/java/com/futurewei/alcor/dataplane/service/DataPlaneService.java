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
package com.futurewei.alcor.dataplane.service;

import com.futurewei.alcor.web.entity.dataplane.InternalDPMResultList;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;

public interface DataPlaneService {

    /**
     * process create network configuration message and send to ACA nodes
     *
     * @param networkConfiguration network configuration details
     * @return InternalDPMResultList result list
     * @throws Exception throw any Exception
     */
    InternalDPMResultList createNetworkConfiguration(NetworkConfiguration networkConfiguration) throws Exception;

    /**
     * process update network configuration message and send to ACA nodes
     *
     * @param networkConfiguration network configuration details
     * @return InternalDPMResultList result list
     * @throws Exception throw any Exception
     */
    InternalDPMResultList updateNetworkConfiguration(NetworkConfiguration networkConfiguration) throws Exception;

    /**
     * process delete network configuration message and send to ACA nodes
     *
     * @param networkConfiguration network configuration details
     * @return InternalDPMResultList result list
     * @throws Exception throw any Exception
     */
    InternalDPMResultList deleteNetworkConfiguration(NetworkConfiguration networkConfiguration) throws Exception;
}
