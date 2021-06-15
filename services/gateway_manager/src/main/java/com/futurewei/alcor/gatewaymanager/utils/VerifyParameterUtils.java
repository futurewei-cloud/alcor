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
package com.futurewei.alcor.gatewaymanager.utils;

import com.futurewei.alcor.gatewaymanager.config.ExceptionMsgConfig;
import com.futurewei.alcor.web.entity.gateway.GatewayEntity;
import com.futurewei.alcor.web.entity.gateway.GatewayInfo;
import com.futurewei.alcor.web.entity.gateway.VpcInfo;
import org.thymeleaf.util.StringUtils;

public class VerifyParameterUtils {

    public static void checkVpcInfo(VpcInfo vpcInfo) throws Exception {
        if (StringUtils.isEmpty(vpcInfo.getVpcId()) || StringUtils.isEmpty(vpcInfo.getOwner()) || vpcInfo.getVpcVni() == null) {
            throw new Exception(ExceptionMsgConfig.VPCINFO_PARAMETER_ILLEGAL.getMsg());
        }
    }

    public static void checkGatewayInfo(GatewayInfo gatewayInfo) throws Exception {
        if (gatewayInfo.getResourceId() == null) {
            throw new Exception(ExceptionMsgConfig.RESOURCE_ID_IS_NULL.getMsg());
        }
        if (gatewayInfo.getGatewayEntities() == null) {
            throw new Exception(ExceptionMsgConfig.GATEWAYS_IS_NULL.getMsg());
        }
        for (GatewayEntity gatewayEntity : gatewayInfo.getGatewayEntities()) {
            if ((gatewayEntity.getType() == null) || StringUtils.isEmpty(gatewayEntity.getStatus())) {
                throw new Exception(ExceptionMsgConfig.GATEWAY_TYPE_OR_STATUS_IS_NULL.getMsg());
            }
        }
    }
}
