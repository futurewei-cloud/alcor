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

package com.futurewei.alcor.elasticipmanager.service.implement;

import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.Ipv4AddrUtil;
import com.futurewei.alcor.common.utils.Ipv6AddrUtil;
import com.futurewei.alcor.elasticipmanager.config.IpVersion;
import com.futurewei.alcor.elasticipmanager.dao.ElasticIpAllocator;
import com.futurewei.alcor.elasticipmanager.dao.ElasticIpRangeRepo;
import com.futurewei.alcor.elasticipmanager.dao.ElasticIpRepo;
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpCommonException;
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpInternalErrorException;
import com.futurewei.alcor.elasticipmanager.exception.elasticip.*;
import com.futurewei.alcor.elasticipmanager.exception.elasticiprange.ElasticIpRangeInUseException;
import com.futurewei.alcor.elasticipmanager.proxy.PortManagerProxy;
import com.futurewei.alcor.elasticipmanager.service.ElasticIpService;
import com.futurewei.alcor.elasticipmanager.utils.ElasticIpControllerUtils;
import com.futurewei.alcor.web.entity.elasticip.ElasticIp;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpRange;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpInfo;
import com.futurewei.alcor.web.entity.port.PortEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;


@Service
@ComponentScan(value = "com.futurewei.alcor.web.restclient")
@ComponentScan(value = "com.futurewei.alcor.common.utils")
public class ElasticIpServiceImpl implements ElasticIpService {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticIpServiceImpl.class);

    @Autowired
    ElasticIpRepo elasticIpRepo;

    @Autowired
    ElasticIpRangeRepo elasticIpRangeRepo;

    @Autowired
    ElasticIpAllocator elasticIpAllocator;

    /**
     * Create an elastic ip.
     * @param request elastic ip configuration
     * @return Elastic ip information
     * @throws ElasticIpRangeInUseException The elastic ip already exist
     * @throws ElasticIpNoProperRangeException There is no elastic ip ranges or the ip version
     *                                         of any ranges is not fit the need
     * @throws ElasticIpInternalErrorException Internal process (database / lock etc.) error
     * @throws ElasticIpAllocationException Allocation failed
     */
    @DurationStatistics
    public ElasticIpInfo createElasticIp(ElasticIpInfo request) throws Exception {
        LOG.debug("Create an elastic ip, request: {}", request);

        ElasticIp eip = new ElasticIp(request);

        if (eip.getId() == null) {
            eip.setId(UUID.randomUUID().toString());
        } else if (elasticIpRepo.findItem(eip.getId()) != null) {
            throw new ElasticIpInUseException();
        }

        Map<String, ElasticIpRange> rangeMap = elasticIpRangeRepo.findAllItems();
        ElasticIpRange range = null;
        for (ElasticIpRange item: rangeMap.values()) {
            if (item.getIpVersion().equals(request.getElasticIpVersion())) {
                range = item;
                break;
            }
        }
        if (range == null) {
            throw new ElasticIpNoProperRangeException();
        }

        eip.setRangeId(range.getId());
        String ipAddress = elasticIpAllocator.allocateIpAddress(range, request.getElasticIp());
        eip.setElasticIp(ipAddress);

        PortEntity port = null;
        try{
            String portId = eip.getPortId();
            if (!StringUtils.isEmpty(portId)) {
                PortManagerProxy portManagerProxy = new PortManagerProxy(request.getProjectId());
                port = portManagerProxy.getPortById(portId);
                String associatedIp = this.getAssociatedPortIp(port, request.getPrivateIp());
                eip.setPrivateIp(associatedIp);
            }

            elasticIpRepo.addItem(eip);
        } catch (Exception e) {
            if (ipAddress != null) {
                elasticIpAllocator.releaseIpAddress(range.getId(), range.getIpVersion(), ipAddress);
            }
            throw e;
        }

        // todo notify nodes

        LOG.debug("Create an elastic ip success, request: {}", request);

        return new ElasticIpInfo(eip, port);
    }

    /**
     * Delete an elastic ip.
     * @param projectId Project the elastic ip belongs to
     * @param elasticIpId Uuid of the elastic ip
     * @throws ElasticIpNotFoundException The elastic ip does not exit
     * @throws ElasticIpInUseException The elastic ip is associated with a port
     * @throws ElasticIpInternalErrorException Internal process (database / lock etc.) error
     */
    @DurationStatistics
    public void deleteElasticIp(String projectId, String elasticIpId) throws Exception {
        LOG.debug("Release an elastic ip, request: {}", elasticIpId);

        ElasticIp eip = elasticIpRepo.findItem(elasticIpId);
        if (eip == null || !projectId.equals(eip.getProjectId())) {
            throw new ElasticIpNotFoundException();
        }

        String portId = eip.getPortId();
        if (!StringUtils.isEmpty(portId)) {
            throw new ElasticIpInUseException();
        }

        elasticIpAllocator.releaseIpAddress(eip.getRangeId(), eip.getElasticIpVersion(), eip.getElasticIp());

        elasticIpRepo.deleteItem(elasticIpId);

        // todo notify nodes

        LOG.debug("Create an elastic ip success, request: {}", elasticIpId);
    }

    /**
     * Get an elastic ip.
     * @param projectId Project the elastic ip belongs to
     * @param elasticIpId Uuid of the elastic ip
     * @return Elastic ip information
     * @throws ElasticIpNotFoundException The elastic ip is not exits
     * @throws ElasticIpInternalErrorException Internal process (database / lock etc.) error
     */
    @DurationStatistics
    public ElasticIpInfo getElasticIp(String projectId, String elasticIpId) throws Exception {
        LOG.debug("Get an elastic ip, request: {}", elasticIpId);

        ElasticIp eip = elasticIpRepo.findItem(elasticIpId);
        if (eip == null) {
            throw new ElasticIpNotFoundException();
        }

        PortEntity port = null;
        if (!StringUtils.isEmpty(eip.getPortId())) {
            PortManagerProxy portManagerProxy = new PortManagerProxy(projectId);
            port = portManagerProxy.getPortById(eip.getPortId());
        }
        ElasticIpInfo result = new ElasticIpInfo(eip, port);

        LOG.debug("Get an elastic ip success, request: {}", elasticIpId);

        return result;
    }

    /**
     * Get fields filtered list of elastic ips belongs to the project.
     * @param projectId Uuid of the project elastic ips belongs to
     * @param queryParams Fields filter map
     * @return A list of elastic ips information
     * @throws ElasticIpInternalErrorException Internal process (database / lock etc.) error
     */
    @DurationStatistics
    public List<ElasticIpInfo> getElasticIps(String projectId, Map<String, Object[]> queryParams) throws Exception {
        LOG.debug("Get elastic ips");

        if (queryParams == null) {
            queryParams = new HashMap<>();
        }

        queryParams.computeIfAbsent("project_id", k -> new String[]{projectId});

        List<ElasticIpInfo> results = new ArrayList<>();
        Map<String, ElasticIp> eips = elasticIpRepo.findAllItems(queryParams);
        for (ElasticIp eipItem: eips.values()) {
            PortEntity port = null;
            if (!StringUtils.isEmpty(eipItem.getPortId())) {
                PortManagerProxy portManagerProxy = new PortManagerProxy(projectId);
                port = portManagerProxy.getPortById(eipItem.getPortId());
            }
            results.add(new ElasticIpInfo(eipItem, port));
        }

        LOG.debug("Get an elastic ip success");

        return results;
    }

    /**
     * Update an elastic ip.
     * @param request elastic ip configuration
     * @return Elastic ip information
     * @throws ElasticIpNotFoundException The elastic ip is not exits
     * @throws ElasticIpAssociatePortException Not allowed to associate a new port when
     *                                         the elastic ip already associated with a port
     * @throws ElasticIpModifyParameterException Not allowed to modify the parameter (elastic_ip etc.)
     * @throws ElasticIpInternalErrorException Internal process (database / lock etc.) error
     */
    @DurationStatistics
    public ElasticIpInfo updateElasticIp(ElasticIpInfo request) throws Exception {
        LOG.debug("Update an elastic ip, request: {}", request);

        ElasticIp eip = elasticIpRepo.findItem(request.getId());
        if (eip == null || !request.getProjectId().equals(eip.getProjectId())) {
            throw new ElasticIpNotFoundException();
        }

        if (!StringUtils.isEmpty(request.getPortId()) && !StringUtils.isEmpty(eip.getPortId()) &&
                !request.getPortId().equals(eip.getPortId())) {
            throw new ElasticIpAssociatePortException();
        }

        if (request.getElasticIpVersion() != null &&
                !request.getElasticIpVersion().equals(eip.getElasticIpVersion())) {
            throw new ElasticIpModifyParameterException();
        }

        if (request.getElasticIp() != null && !request.getElasticIp().equals(eip.getElasticIp())) {
            throw new ElasticIpModifyParameterException();
        }

        if (request.getPrivateIp() != null && eip.getPrivateIp() != null &&
                !request.getPrivateIp().equals(eip.getPrivateIp())) {
            throw new ElasticIpModifyParameterException();
        }

        String newPortId = request.getPortId();
        PortEntity port = null;
        if (newPortId != null) {
            eip.setPortId(newPortId);
            if (newPortId.isEmpty()) {
                eip.setPrivateIp(null);
            } else {
                PortManagerProxy portManagerProxy = new PortManagerProxy(request.getProjectId());
                port = portManagerProxy.getPortById(newPortId);
                String associatedIp = this.getAssociatedPortIp(port, request.getPrivateIp());
                eip.setPrivateIp(associatedIp);
            }
        }

        if (request.getDescription() != null) {
            eip.setDescription(request.getDescription());
        }

        if (request.getName() != null) {
            eip.setName(request.getName());
        }

        if (request.getDnsDomain() != null) {
            eip.setDnsDomain(request.getDnsDomain());
        }

        if (request.getDnsName() != null) {
            eip.setDnsName(request.getDnsName());
        }

        elasticIpRepo.addItem(eip);

        // todo notify nodes

        LOG.debug("Update an elastic ip success, request: {}", request);

        return new ElasticIpInfo(eip, port);
    }

    private Map<String, ElasticIp> getElasticIpsByPortId(String projectId, String portId) {

        Map<String, Object[]> filter = new HashMap<>();
        filter.put("project_id", new Object[] {projectId});
        filter.put("port_id", new Object[] {portId});

        return elasticIpRepo.findAllItems(filter);
    }

    private String getAssociatedPortIp(PortEntity port, String ipAddress)
            throws Exception {

        List<PortEntity.FixedIp> fixedIps = port.getFixedIps();
        List<String> fixedIpList = new ArrayList<>();
        for (PortEntity.FixedIp fixedIp: fixedIps) {
            fixedIpList.add(fixedIp.getIpAddress());
        }

        if (fixedIpList.isEmpty()) {
            throw new ElasticIpPipNotFound();
        }

        Map<String, ElasticIp> associatedEips = this.getElasticIpsByPortId(port.getProjectId(), port.getId());
        String associatedIp;
        if (ipAddress != null) {
            if (fixedIpList.contains(ipAddress)) {
                associatedIp = ipAddress;
            } else {
                throw new ElasticIpPipNotFound();
            }
            for (ElasticIp eip: associatedEips.values()) {
                if (associatedIp.equals(eip.getPrivateIp())) {
                    String errorMessage = "The associate port or private ip has already associated with " +
                            "another elastic ip: " + eip.getElasticIp();
                    throw new ElasticIpCommonException(HttpStatus.BAD_REQUEST, errorMessage);
                }
            }
        } else {
            // choose a not associated fixed ip if not specified
            for (ElasticIp eip: associatedEips.values()) {
                fixedIpList.remove(eip.getPrivateIp());
            }
            if (fixedIpList.isEmpty()) {
                throw new ElasticIpPipNotFound();
            } else {
                associatedIp = fixedIpList.get(0);
            }
        }

        for (ElasticIp eip: associatedEips.values()) {
            if (associatedIp.equals(eip.getPrivateIp())) {
                String errorMessage = "The associate port or private ip has already associated with " +
                        "another elastic ip: " + eip.getElasticIp();
                throw new ElasticIpCommonException(HttpStatus.BAD_REQUEST, errorMessage);
            }
        }

        return associatedIp;
    }

    public List<ElasticIpInfo> createElasticIps(List<ElasticIpInfo> bulkRequest) throws Exception {
        // todo
        return new ArrayList<>();
    }

    public void deleteElasticIps(String projectId, List<String> bulkRequest) throws Exception {
        // todo
    }
}
