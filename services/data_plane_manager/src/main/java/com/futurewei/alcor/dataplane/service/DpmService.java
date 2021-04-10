/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.dataplane.service;

import com.futurewei.alcor.web.entity.dataplane.InternalDPMResultList;
import com.futurewei.alcor.web.entity.dataplane.v2.NetworkConfiguration;

public interface DpmService {

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
