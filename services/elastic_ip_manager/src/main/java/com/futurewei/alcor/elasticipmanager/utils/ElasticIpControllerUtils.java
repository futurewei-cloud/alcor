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

package com.futurewei.alcor.elasticipmanager.utils;

import com.futurewei.alcor.common.utils.Ipv4AddrUtil;
import com.futurewei.alcor.common.utils.Ipv6AddrUtil;
import com.futurewei.alcor.elasticipmanager.config.IpVersion;
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpIdConfilictException;
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpNoProjectIdException;
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpProjectIdConflictException;
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpQueryFormatException;
import com.futurewei.alcor.elasticipmanager.exception.elasticip.*;
import com.futurewei.alcor.elasticipmanager.exception.elasticiprange.ElasticIpRangeBadRangesException;
import com.futurewei.alcor.elasticipmanager.exception.elasticiprange.ElasticIpRangeNoIdException;
import com.futurewei.alcor.elasticipmanager.exception.elasticiprange.ElasticIpRangeVersionException;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpInfo;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpRange;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpRangeInfo;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.util.*;

public class ElasticIpControllerUtils {


    public static boolean isIpAddressInvalid(String ipAddress) {
        boolean isInvalid = !Ipv4AddrUtil.formatCheck(ipAddress);
        if (isInvalid) {
            isInvalid = !Ipv6AddrUtil.formatCheck(ipAddress);
        }

        return isInvalid;
    }

    public static boolean isIpVersionInvalid(Integer ipVersion) {
        return !ipVersion.equals(IpVersion.IPV4.getVersion()) && !ipVersion.equals(IpVersion.IPV6.getVersion());
    }

    public static boolean isAllocationRangesInvalid(Integer ipVersion,
                                                    List<ElasticIpRange.AllocationRange> allocationRanges) {

        boolean isInvalid = false;
        if (allocationRanges != null) {

            try {
                if (ipVersion.equals(IpVersion.IPV4.getVersion())) {
                    for (ElasticIpRange.AllocationRange range: allocationRanges) {
                        long start = Ipv4AddrUtil.ipv4ToLong(range.getStart());
                        long end = Ipv4AddrUtil.ipv4ToLong(range.getEnd());
                        if (start > end) {
                            isInvalid = true;
                        }
                    }
                } else if (ipVersion.equals(IpVersion.IPV6.getVersion())) {
                    for (ElasticIpRange.AllocationRange range: allocationRanges) {
                        BigInteger start = Ipv6AddrUtil.ipv6ToBitInt(range.getStart());
                        BigInteger end = Ipv6AddrUtil.ipv6ToBitInt(range.getEnd());
                        if (start.compareTo(end) > 0) {
                            isInvalid = true;
                        }
                    }
                }
            } catch (NumberFormatException e) {
                isInvalid = true;
            }
        }
        return isInvalid;
    }

    public static void createElasticIpParameterProcess(String projectId, ElasticIpInfo elasticIpInfo)
            throws Exception {

        if (elasticIpInfo == null) {
            throw new ElasticIpQueryFormatException();
        }

        if (StringUtils.isEmpty(projectId)) {
            throw new ElasticIpNoProjectIdException();
        } else if (elasticIpInfo.getProjectId() == null) {
            elasticIpInfo.setProjectId(projectId);
        } else if (!projectId.equals(elasticIpInfo.getProjectId())) {
            throw new ElasticIpProjectIdConflictException();
        }

        if (elasticIpInfo.getElasticIpVersion() == null) {
            elasticIpInfo.setElasticIpVersion(IpVersion.IPV4.getVersion());
        } else if (isIpVersionInvalid(elasticIpInfo.getElasticIpVersion())) {
            throw new ElasticIpEipVersionException();
        }

        if (elasticIpInfo.getElasticIp() != null) {
            if (isIpAddressInvalid(elasticIpInfo.getElasticIp())) {
                throw new ElasticIpEipAddressException();
            }
        }

        if (elasticIpInfo.getPortId() == null) {
            elasticIpInfo.setPortId("");
        }
        if (elasticIpInfo.getPortId().isEmpty()) {
            if (elasticIpInfo.getPrivateIp()!= null) {
                throw new ElasticIpNoPortIdException();
            }
        } else {
            if (elasticIpInfo.getPrivateIp() != null) {
                if (isIpAddressInvalid(elasticIpInfo.getPrivateIp())) {
                    throw new ElasticIpPipAddressException();
                }
            }
        }

        if (elasticIpInfo.getName() == null) {
            elasticIpInfo.setName("");
        }

        if (elasticIpInfo.getDescription() == null) {
            elasticIpInfo.setDescription("");
        }

        if (elasticIpInfo.getDnsDomain() == null) {
            elasticIpInfo.setDnsDomain("");
        }

        if (elasticIpInfo.getDnsName() == null) {
            elasticIpInfo.setDnsName("");
        }
    }

    public static void updateElasticIpParameterProcess(String projectId, String elasticIpId,
                                                       ElasticIpInfo elasticIpInfo) throws Exception {

        if (elasticIpInfo == null) {
            throw new ElasticIpQueryFormatException();
        }

        if (StringUtils.isEmpty(projectId)) {
            throw new ElasticIpNoProjectIdException();
        } else if (elasticIpInfo.getProjectId() == null) {
            elasticIpInfo.setProjectId(projectId);
        } else if (!projectId.equals(elasticIpInfo.getProjectId())) {
            throw new ElasticIpProjectIdConflictException();
        }

        if (StringUtils.isEmpty(elasticIpId)) {
            throw new ElasticIpNoIdException();
        } else if (elasticIpInfo.getId() == null) {
            elasticIpInfo.setId(elasticIpId);
        } else if (!elasticIpId.equals(elasticIpInfo.getId())) {
            throw new ElasticIpIdConfilictException();
        }

        if (elasticIpInfo.getElasticIpVersion() != null &&
                isIpVersionInvalid(elasticIpInfo.getElasticIpVersion())) {
            throw new ElasticIpEipVersionException();
        }

        if (elasticIpInfo.getElasticIp() != null) {
            if (elasticIpInfo.getElasticIpVersion() == null) {
                elasticIpInfo.setElasticIpVersion(IpVersion.IPV4.getVersion());
            }
            if (isIpAddressInvalid(elasticIpInfo.getElasticIp())) {
                throw new ElasticIpEipAddressException();
            }
        }

        if (StringUtils.isEmpty(elasticIpInfo.getPortId())) {
            if (elasticIpInfo.getPrivateIp() != null) {
                throw new ElasticIpNoPortIdException();
            }
        } else {
            if (elasticIpInfo.getPrivateIp() != null) {
                if (isIpAddressInvalid(elasticIpInfo.getPrivateIp())) {
                    throw new ElasticIpPipAddressException();
                }
            }
        }
    }

    public static void createElasticIpRangeParameterProcess(ElasticIpRangeInfo elasticIpRangeInfo)
            throws Exception {

        if (elasticIpRangeInfo == null) {
            throw new ElasticIpQueryFormatException();
        }

        if (elasticIpRangeInfo.getIpVersion() == null) {
            elasticIpRangeInfo.setIpVersion(IpVersion.IPV4.getVersion());
        } else if (isIpVersionInvalid(elasticIpRangeInfo.getIpVersion())) {
            throw new ElasticIpRangeVersionException();
        }

        List<ElasticIpRange.AllocationRange> allocationRanges = elasticIpRangeInfo.getAllocationRanges();
        if (allocationRanges != null) {
            if (isAllocationRangesInvalid(elasticIpRangeInfo.getIpVersion(), allocationRanges)) {
                throw new ElasticIpRangeBadRangesException();
            }
        } else {
            elasticIpRangeInfo.setAllocationRanges(new ArrayList<>());
        }

        if (elasticIpRangeInfo.getName() == null) {
            elasticIpRangeInfo.setName("");
        }

        if (elasticIpRangeInfo.getDescription() == null) {
            elasticIpRangeInfo.setDescription("");
        }
    }

    public static void updateElasticIpRangeParameterProcess(String elasticipRangeId,
                                                            ElasticIpRangeInfo elasticIpRangeInfo) throws Exception {

        if (elasticIpRangeInfo == null) {
            throw new ElasticIpQueryFormatException();
        }

        if (StringUtils.isEmpty(elasticipRangeId)) {
            throw new ElasticIpRangeNoIdException();
        } else if (elasticIpRangeInfo.getId() == null) {
            elasticIpRangeInfo.setId(elasticipRangeId);
        } else if (!elasticipRangeId.equals(elasticIpRangeInfo.getId())) {
            throw new ElasticIpIdConfilictException();
        }

        if (elasticIpRangeInfo.getIpVersion() != null && isIpVersionInvalid(elasticIpRangeInfo.getIpVersion())) {
            throw new ElasticIpRangeVersionException();
        }

        List<ElasticIpRange.AllocationRange> allocationRanges = elasticIpRangeInfo.getAllocationRanges();
        if (allocationRanges != null) {
            if (elasticIpRangeInfo.getIpVersion() == null) {
                elasticIpRangeInfo.setIpVersion(IpVersion.IPV4.getVersion());
            }
            if (isAllocationRangesInvalid(elasticIpRangeInfo.getIpVersion(), allocationRanges)) {
                throw new ElasticIpRangeBadRangesException();
            }
        }
    }

    public static int getVersionByIpString (String ipAddress) throws Exception {
        if (Ipv4AddrUtil.formatCheck(ipAddress)) {
            return IpVersion.IPV4.getVersion();
        } else if (Ipv6AddrUtil.formatCheck(ipAddress)) {
            return IpVersion.IPV6.getVersion();
        }

        throw new Exception("The ip address is invalid");
    }
}
