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

import com.futurewei.alcor.elasticipmanager.dao.ElasticIpAllocator;
import com.futurewei.alcor.elasticipmanager.dao.ElasticIpRangeRepo;
import com.futurewei.alcor.elasticipmanager.dao.ElasticIpRepo;
import com.futurewei.alcor.elasticipmanager.exception.elasticip.ElasticIpAllocationException;
import com.futurewei.alcor.elasticipmanager.exception.elasticip.ElasticIpInUseException;
import com.futurewei.alcor.elasticipmanager.exception.elasticip.ElasticIpNotFoundException;
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpQueryFormatException;
import com.futurewei.alcor.elasticipmanager.exception.elasticiprange.ElasticIpRangeInUseException;
import com.futurewei.alcor.elasticipmanager.exception.elasticiprange.ElasticIpRangeNotFoundException;
import com.futurewei.alcor.elasticipmanager.service.ElasticIpService;
import com.futurewei.alcor.web.entity.elasticip.ElasticIp;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpRange;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;


@Service
public class ElasticIpServiceImpl implements ElasticIpService {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticIpServiceImpl.class);

    @Autowired
    ElasticIpRepo elasticIpRepo;

    @Autowired
    ElasticIpRangeRepo elasticIpRangeRepo;

    @Autowired
    ElasticIpAllocator elasticIpAllocator;

    public ElasticIpInfo createElasticIp(ElasticIpInfo request) throws Exception {
        LOG.debug("Create an elastic ip, request: {}", request);

        String rangeId = request.getRangeId();
        ElasticIpRange range = elasticIpRangeRepo.findItem(rangeId);
        if (range == null) {
            throw new ElasticIpRangeNotFoundException();
        }
        if (!range.getIpVersion().equals(request.getElasticIpVersion())) {
            throw new ElasticIpQueryFormatException();
        }

        ElasticIp eip = new ElasticIp(request);

        if (eip.getId() == null) {
            eip.setId(UUID.randomUUID().toString());
        } else if (elasticIpRepo.findItem(eip.getId()) != null) {
            throw new ElasticIpRangeInUseException();
        }


        String ipAddress = elasticIpAllocator.allocateIpAddress(range, request.getElasticIp());
        if (ipAddress == null) {
            throw new ElasticIpAllocationException();
        }
        eip.setElasticIp(ipAddress);

        String portId = eip.getPortId();
        if (portId != null) {
            // todo query port for verify

        }

        elasticIpRepo.addItem(eip);

        // todo notify nodes

        LOG.debug("Create an elastic ip success, request: {}", request);

        return new ElasticIpInfo(eip);
    }

    public void deleteElasticIp(String projectId, String elasticIpId) throws Exception {
        LOG.debug("Release an elastic ip, request: {}", elasticIpId);

        ElasticIp eip = elasticIpRepo.findItem(elasticIpId);
        if (eip == null || !projectId.equals(eip.getProjectId())) {
            throw new ElasticIpNotFoundException();
        }

        String portId = eip.getPortId();
        if (portId != null) {
            throw new ElasticIpInUseException();
        }

        elasticIpAllocator.releaseIpAddress(eip.getRangeId(), eip.getElasticIpVersion(), eip.getElasticIp());

        elasticIpRepo.deleteItem(elasticIpId);

        // todo notify nodes

        LOG.debug("Create an elastic ip success, request: {}", elasticIpId);
    }

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

    public List<ElasticIpInfo> getElasticIps(String projectId) throws Exception {
        LOG.debug("Get elastic ips");

        List<ElasticIpInfo> results = new ArrayList<>();
        Map<String, ElasticIp> eips = elasticIpRepo.findAllItems();
        for (ElasticIp eipItem: eips.values()) {
            if (projectId.equals(eipItem.getProjectId())) {
                results.add(new ElasticIpInfo(eipItem));
            }
        }

        LOG.debug("Get an elastic ip success");

        return results;
    }

    public ElasticIpInfo updateElasticIp(ElasticIpInfo request) throws Exception {
        LOG.debug("Update an elastic ip, request: {}", request);

        ElasticIp eip = elasticIpRepo.findItem(request.getId());
        if (eip == null || !request.getProjectId().equals(eip.getProjectId())) {
            throw new ElasticIpNotFoundException();
        }

        boolean validCheck = true;
        if (request.getElasticIpVersion() != null &&
                !request.getElasticIpVersion().equals(eip.getElasticIpVersion())) {
            LOG.debug("Modify IP version of an elastic ip is not allowed, request: {}", request);
            validCheck = false;
        } else if (request.getElasticIp() != null && !request.getElasticIp().equals(eip.getElasticIp())) {
            LOG.debug("Modify the address of an elastic ip is not allowed, request: {}", request);
            validCheck = false;
        } else if (request.getRangeId() != null && !request.getRangeId().equals(eip.getRangeId())) {
            LOG.debug("Modify the range id of an elastic ip is not allowed, request: {}", request);
            validCheck = false;
        } else if (request.getPortId() != null && eip.getPortId() != null &&
                !request.getPortId().equals(eip.getPortId())) {
            LOG.debug("Should disassociate with the port before associate a new port, request: {}", request);
            validCheck = false;
        } else if (request.getPrivateIpVersion() != null && eip.getPrivateIpVersion() != null &&
                !request.getPrivateIpVersion().equals(eip.getElasticIpVersion())) {
            LOG.debug("Modify the bound private ip of an elastic ip is not allowed , request: {}", request);
            validCheck = false;
        } else if (request.getPrivateIp() != null && eip.getPrivateIp() != null &&
                !request.getPrivateIp().equals(eip.getPrivateIp())) {
            LOG.debug("Modify the bound private ip of an elastic ip is not allowed , request: {}", request);
            validCheck = false;
        }

        if (!validCheck) {
            throw new ElasticIpQueryFormatException();
        }

        String newPortId = request.getPortId();
        if (newPortId != null) {
            eip.setPortId(newPortId);
            if (newPortId.isEmpty()) {
                eip.setPrivateIp("");
            } else {
                // todo query port for verify

                if (request.getPrivateIp() != null) {
                    eip.setPrivateIp(request.getPrivateIp());
                    eip.setPrivateIpVersion(request.getPrivateIpVersion());
                }
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

    public List<ElasticIpInfo> createElasticIps(List<ElasticIpInfo> bulkRequest) throws Exception {
        // todo
        return new ArrayList<>();
    }

    public void deleteElasticIps(String projectId, List<String> bulkRequest) throws Exception {
        // todo
    }
}
