package com.futurewei.alcor.web.entity.gateway;


import lombok.Data;

import java.io.Serializable;

@Data
public class GatewayIp implements Serializable {
    private String ip;
    private String mac;
}
