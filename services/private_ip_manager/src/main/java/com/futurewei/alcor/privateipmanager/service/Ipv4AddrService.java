package com.futurewei.alcor.privateipmanager.service;

import com.futurewei.alcor.privateipmanager.http.Ipv4AddrRangeRequest;
import com.futurewei.alcor.privateipmanager.http.Ipv4AddrRequest;
import com.futurewei.alcor.privateipmanager.http.Ipv4AddrRequestBulk;

import java.util.Map;

public interface Ipv4AddrService {

    Ipv4AddrRequest allocateIpv4Addr(Ipv4AddrRequest request) throws Exception;

    Ipv4AddrRequestBulk allocateIpv4AddrBulk(Ipv4AddrRequestBulk requestBulk);

    Ipv4AddrRequest modifyIpv4AddrState(Ipv4AddrRequest request) throws Exception;

    Ipv4AddrRequestBulk modifyIpv4AddrStateBulk(Ipv4AddrRequestBulk requestBulk);

    Ipv4AddrRequest releaseIpv4Addr(String subnetId, String ipv4Addr) throws Exception;

    Ipv4AddrRequestBulk releaseIpv4AddrBulk(Ipv4AddrRequestBulk requestBulk);

    Ipv4AddrRequest getIpv4Addr(String subnetId, String ipv4Addr) throws Exception;

    Map listAllocatedIpv4Addr();

    Ipv4AddrRangeRequest createIpv4AddrRange(Ipv4AddrRangeRequest request) throws Exception;

    Ipv4AddrRangeRequest deleteIpv4AddrRange(String subnetId) throws Exception;

    Ipv4AddrRangeRequest getIpv4AddrRange(String subnetId) throws Exception;

    Map listIpv4AddrRange();
}
