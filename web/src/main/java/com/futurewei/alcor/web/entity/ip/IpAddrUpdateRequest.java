package com.futurewei.alcor.web.entity.ip;

import lombok.Data;

import java.util.List;

@Data
public class IpAddrUpdateRequest {
    private List<IpAddrRequest> oldIpAddrRequests;
    private List<IpAddrRequest> newIpAddrRequests;
}
