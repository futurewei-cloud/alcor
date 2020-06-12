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
package com.futurewei.alcor.portmanager.dao;

import com.futurewei.alcor.web.entity.port.PortEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class PortDao {
    @Autowired
    private PortEntityDao portEntityDao;

    @Autowired
    private AllowAddressPairDao allowAddressPairDao;

    @Autowired
    private DnsRecordDao dnsRecordDao;

    @Autowired
    private ExtraDhcpOptDao extraDhcpOptDao;

    @Autowired
    private FixedIpDao fixedIpDao;

    @Autowired
    private PortSecurityGroupDao portSecurityGroupDao;

    @Autowired
    private PortTagDao portTagDao;


    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout=36000, rollbackFor=Exception.class)
    public void createPort(PortEntity portEntity) throws Exception {
        portEntityDao.insertPort(portEntity);

        if (portEntity.getId() != null) {
            throw new Exception("test");
        }

        if (portEntity.getAllowedAddressPairs() != null) {
            allowAddressPairDao.insertAllowAddressPairs(portEntity.getId(), portEntity.getAllowedAddressPairs());
        }

        if (portEntity.getDnsAssignment() != null) {
            dnsRecordDao.insertDnsRecords(portEntity.getId(), portEntity.getDnsAssignment());
        }

        if (portEntity.getExtraDhcpOpts() != null) {
            extraDhcpOptDao.insertExtraDhcpOpts(portEntity.getId(), portEntity.getExtraDhcpOpts());
        }

        if (portEntity.getFixedIps() != null) {
            fixedIpDao.insertFixedIps(portEntity.getId(), portEntity.getFixedIps());
        }

        if (portEntity.getSecurityGroups() != null) {
            portSecurityGroupDao.insertPortSecurityGroups(portEntity.getId(), portEntity.getSecurityGroups());
        }

        if (portEntity.getTags() != null) {
            portTagDao.insertPortTags(portEntity.getId(), portEntity.getTags());
        }
    }

    @Transactional
    public void createPorts(List<PortEntity> portEntities) {


        portEntityDao.insertPorts(portEntities);
        //allowAddressPairDao.insertAllowAddressPairs(portEntity.getId(), portEntity.getAllowedAddressPairs());
    }

    @Transactional
    public void updatePort(PortEntity portEntity) {
        portEntityDao.insertPort(portEntity);
        allowAddressPairDao.insertAllowAddressPairs(portEntity.getId(), portEntity.getAllowedAddressPairs());
    }

    @Transactional
    public void deletePort(String portId) {
        portEntityDao.deletePort(portId);
        //Delete...
    }

    public PortEntity findPort(String portId) {
        return portEntityDao.findPortById(portId);
    }

    public List<PortEntity> listPort() {
        return portEntityDao.listPort();
    }
}
