package com.futurewei.alcor.privateipmanager.service;

import com.futurewei.alcor.privateipmanager.entity.IpAddrRangeRequest;
import com.futurewei.alcor.privateipmanager.entity.IpAddrRequest;
import com.futurewei.alcor.privateipmanager.entity.IpAddrRequestBulk;

import java.util.List;

public interface IpAddrService {

    IpAddrRequest allocateIpAddr(IpAddrRequest request) throws Exception;

    IpAddrRequestBulk allocateIpAddrBulk(IpAddrRequestBulk requestBulk) throws Exception;

    IpAddrRequest modifyIpAddrState(IpAddrRequest request) throws Exception;

    IpAddrRequestBulk modifyIpAddrStateBulk(IpAddrRequestBulk requestBulk) throws Exception;

    IpAddrRequest releaseIpAddr(int ipVersion, String subnetId, String ipAddr) throws Exception;

    IpAddrRequestBulk releaseIpAddrBulk(IpAddrRequestBulk requestBulk) throws Exception;

    IpAddrRequest getIpAddr(int ipVersion, String subnetId, String ipAddr) throws Exception;

    List<IpAddrRequest> getIpAddrBulk(String subnetId) throws Exception;

    IpAddrRangeRequest createIpAddrRange(IpAddrRangeRequest request) throws Exception;

    IpAddrRangeRequest deleteIpAddrRange(String subnetId) throws Exception;

    IpAddrRangeRequest getIpAddrRange(String subnetId) throws Exception;

    List<IpAddrRangeRequest> listIpAddrRange();
}
