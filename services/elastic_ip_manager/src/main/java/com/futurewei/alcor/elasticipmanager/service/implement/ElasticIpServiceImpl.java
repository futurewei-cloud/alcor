/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/

package com.futurewei.alcor.elasticipmanager.service.implement;

import com.futurewei.alcor.common.utils.Ipv4AddrUtil;
import com.futurewei.alcor.elasticipmanager.dao.ElasticIpAllocator;
import com.futurewei.alcor.elasticipmanager.dao.ElasticIpRangeRepo;
import com.futurewei.alcor.elasticipmanager.dao.ElasticIpRepo;
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

        String portId = eip.getPortId();
        if (!StringUtils.isEmpty(portId)) {
            String associatedIp = this.getAssociatedPortIp(request.getProjectId(), portId,
                    request.getPrivateIpVersion(), request.getPrivateIp());
            eip.setPrivateIp(associatedIp);
            eip.setPrivateIpVersion(ElasticIpControllerUtils.getVersionByIpString(associatedIp));
        }

        elasticIpRepo.addItem(eip);

        // todo notify nodes

        LOG.debug("Create an elastic ip success, request: {}", request);

        return new ElasticIpInfo(eip);
    }

    /**
     * Delete an elastic ip.
     * @param projectId Project the elastic ip belongs to
     * @param elasticIpId Uuid of the elastic ip
     * @throws ElasticIpNotFoundException The elastic ip does not exit
     * @throws ElasticIpInUseException The elastic ip is associated with a port
     * @throws ElasticIpInternalErrorException Internal process (database / lock etc.) error
     */
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
    public ElasticIpInfo getElasticIp(String projectId, String elasticIpId) throws Exception {
        LOG.debug("Get an elastic ip, request: {}", elasticIpId);

        ElasticIp eip = elasticIpRepo.findItem(elasticIpId);
        if (eip == null || !projectId.equals(eip.getProjectId())) {
            throw new ElasticIpNotFoundException();
        }
        ElasticIpInfo result = new ElasticIpInfo(eip);

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
    public List<ElasticIpInfo> getElasticIps(String projectId, Map<String, Object[]> queryParams) throws Exception {
        LOG.debug("Get elastic ips");

        if (queryParams == null) {
            queryParams = new HashMap<>();
        }

        queryParams.computeIfAbsent("project_id", k -> new String[]{projectId});

        List<ElasticIpInfo> results = new ArrayList<>();
        Map<String, ElasticIp> eips = elasticIpRepo.findAllItems(queryParams);
        for (ElasticIp eipItem: eips.values()) {
            results.add(new ElasticIpInfo(eipItem));
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

        if (request.getPrivateIpVersion() != null && eip.getPrivateIpVersion() != null &&
                !request.getPrivateIpVersion().equals(eip.getPrivateIpVersion())) {
            throw new ElasticIpModifyParameterException();
        }

        if (request.getPrivateIp() != null && eip.getPrivateIp() != null &&
                !request.getPrivateIp().equals(eip.getPrivateIp())) {
            throw new ElasticIpModifyParameterException();
        }

        String newPortId = request.getPortId();
        if (newPortId != null) {
            eip.setPortId(newPortId);
            if (newPortId.isEmpty()) {
                eip.setPrivateIpVersion(null);
                eip.setPrivateIp(null);
            } else {
                String associatedIp = this.getAssociatedPortIp(request.getProjectId(), newPortId,
                        request.getPrivateIpVersion(), request.getPrivateIp());
                eip.setPrivateIp(associatedIp);
                eip.setPrivateIpVersion(ElasticIpControllerUtils.getVersionByIpString(associatedIp));
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

        return new ElasticIpInfo(eip);
    }

    private Map<String, ElasticIp> getElasticIpsByPortId(String projectId, String portId) {

        Map<String, Object[]> filter = new HashMap<>();
        filter.put("project_id", new Object[] {projectId});
        filter.put("port_id", new Object[] {portId});

        return elasticIpRepo.findAllItems(filter);
    }

    private String getAssociatedPortIp(String projectId, String portId, Integer ipVersion, String ipAddress)
            throws Exception {
        PortManagerProxy portManagerProxy = new PortManagerProxy(projectId);
        PortEntity port = portManagerProxy.getPortById(portId);

        List<PortEntity.FixedIp> fixedIps = port.getFixedIps();
        List<String> fixedIpv4List = new ArrayList<>();
        for (PortEntity.FixedIp fixedIp: fixedIps) {
            if (Ipv4AddrUtil.formatCheck(fixedIp.getIpAddress())) {
                fixedIpv4List.add(fixedIp.getIpAddress());
            }
        }

        if (fixedIpv4List.isEmpty()) {
            throw new ElasticIpPipNotFound();
        }

        String associatedIp;
        if (ipAddress != null) {
            if (fixedIpv4List.contains(ipAddress)) {
                associatedIp = ipAddress;
            } else {
                throw new ElasticIpPipNotFound();
            }
        } else {
            if (fixedIpv4List.size() > 1) {
                throw new ElasticIpMultipleFixedIpFound();
            }
            associatedIp = fixedIpv4List.get(0);
        }

        Map<String, ElasticIp> associatedEips = this.getElasticIpsByPortId(projectId, projectId);
        for (ElasticIp eip: associatedEips.values()) {
            if (associatedIp.equals(eip.getPrivateIp())) {
                throw new ElasticIpAssociateConflict();
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
