package com.futurewei.alcor.privateipmanager.service;

import com.futurewei.alcor.web.entity.ip.*;

import java.util.List;

public interface IpAddrService {

    IpAddrRequest allocateIpAddr(IpAddrRequest request) throws Exception;

    IpAddrRequestBulk allocateIpAddrBulk(IpAddrRequestBulk requestBulk) throws Exception;

    IpAddrRequest modifyIpAddrState(IpAddrRequest request) throws Exception;

    IpAddrRequestBulk modifyIpAddrStateBulk(IpAddrRequestBulk requestBulk) throws Exception;

    void releaseIpAddr(String rangeId, String ipAddr) throws Exception;

    void releaseIpAddrBulk(IpAddrRequestBulk requestBulk) throws Exception;

    IpAddrRequest getIpAddr(String rangeId, String ipAddr) throws Exception;

    List<IpAddrRequest> getIpAddrBulk(String rangeId) throws Exception;

    IpAddrRangeRequest createIpAddrRange(IpAddrRangeRequest request) throws Exception;

    void deleteIpAddrRange(String rangeId) throws Exception;

    IpAddrRangeRequest getIpAddrRange(String rangeId) throws Exception;

    List<IpAddrRangeRequest> listIpAddrRange();

    List<IpAddrRequest> updateIpAddr(IpAddrUpdateRequest request) throws Exception;
}
