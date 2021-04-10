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

package com.futurewei.alcor.vpcmanager.service;

import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.vpcmanager.exception.SubnetsNotEmptyException;
import com.futurewei.alcor.web.entity.route.RouteWebJson;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

public interface VpcService {

    public RouteWebJson getRoute (String vpcId, VpcEntity vpcState);

    public VpcEntity allocateSegmentForNetwork (VpcEntity vpcEntity) throws Exception;

    public boolean checkSubnetsAreEmpty (VpcEntity vpcEntity) throws SubnetsNotEmptyException;

    public ResponseId registerVpc(VpcEntity vpcEntity);

    public ResponseId unRegisterVpc(VpcEntity vpcEntity);
}
