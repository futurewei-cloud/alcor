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
