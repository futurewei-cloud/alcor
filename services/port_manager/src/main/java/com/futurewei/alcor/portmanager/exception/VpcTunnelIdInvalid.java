package com.futurewei.alcor.portmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "[Port Creation] Invalid VPC tunnelId")
public class VpcTunnelIdInvalid extends Exception {

    public VpcTunnelIdInvalid() {
    }

    public VpcTunnelIdInvalid(String vpcId, Integer tenantId) {
        super("[Port Creation] Invalid VPC tunnelId: " + tenantId + " | vpcId: " + vpcId);
    }

}
