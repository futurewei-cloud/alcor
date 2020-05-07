package com.futurewei.alcor.vpcmanager.service.Impl;

import com.futurewei.alcor.common.constants.NetworkType;
import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.NetworkTypeInvalidException;
import com.futurewei.alcor.common.exception.VlanRangeNotFoundException;
import com.futurewei.alcor.vpcmanager.dao.GreRepository;
import com.futurewei.alcor.vpcmanager.dao.VlanRangeRepository;
import com.futurewei.alcor.vpcmanager.dao.VlanRepository;
import com.futurewei.alcor.vpcmanager.dao.VxlanRepository;
import com.futurewei.alcor.vpcmanager.service.SegmentService;
import com.futurewei.alcor.web.allocator.VlanKeyAllocator;
import com.futurewei.alcor.web.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SegmentServiceImpl implements SegmentService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private VlanRangeRepository vlanRangeRepository;

    @Autowired
    private VlanRepository vlanRepository;

    @Autowired
    private VxlanRepository vxlanRepository;

    @Autowired
    private GreRepository greRepository;

    @Override
    public Long addVlanEntity(String segmentId, String vlanId, String networkType) throws DatabasePersistenceException, CacheException {
        try {
            NetworkVlanType vlan = new NetworkVlanType();
            // check if range exist in db
            Map<String, NetworkVlanRange> map = this.vlanRangeRepository.findAllItems();
            if (map.size() == 0) {
                String rangeId = UUID.randomUUID().toString();
                NetworkVlanRangeRequest request = new NetworkVlanRangeRequest(rangeId, segmentId, networkType, NetworkType.VLAN_FIRST_KEY, NetworkType.VLAN_LAST_KEY);
                this.vlanRangeRepository.createRange(request);
            }

            String rangeId = "";
            for (Map.Entry<String, NetworkVlanRange> entry : map.entrySet()) {
                rangeId = entry.getKey();
            }

            Long key = this.vlanRangeRepository.allocateVlanKey(rangeId);

            //vlan.setMtu(mtu);
            vlan.setSegmentId(segmentId);
            vlan.setVlanId(vlanId);
            vlan.setKey(key);

            this.vlanRepository.addItem(vlan);
            logger.info("Allocate vlan key success, key: " + key);
            return key;
        } catch (Exception e) {
            this.vlanRepository.deleteItem(vlanId);
            logger.info("Allocate vlan key failed");
            throw new DatabasePersistenceException(e.getMessage());
        }

    }

    @Override
    public Long addVxlanEntity(String segmentId) throws DatabasePersistenceException {
        try {
            String vxlanId = UUID.randomUUID().toString();
            NetworkVxlanType vxlan = new NetworkVxlanType();
            //vxlan.setMtu(mtu);
            vxlan.setSegmentId(segmentId);
            vxlan.setVxlanId(vxlanId);

            this.vxlanRepository.addItem(vxlan);
            return null;
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    public Long addGreEntity(String segmentId) throws DatabasePersistenceException {
        try {
            String greId = UUID.randomUUID().toString();
            NetworkVGREType gre = new NetworkVGREType();
            gre.setSegmentId(segmentId);
            gre.setGreId(greId);

            this.greRepository.addItem(gre);
            return null;
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    public void releaseVlanEntity(String vlanId, Long key) throws DatabasePersistenceException {
        try {
            Map<String, NetworkVlanRange> map = this.vlanRangeRepository.findAllItems();
            if (map.size() == 0) {
                // TO DO: throw exception
                return;
            }

            String rangeId = "";
            for (Map.Entry<String, NetworkVlanRange> entry : map.entrySet()) {
                rangeId = entry.getKey();
            }

            this.vlanRangeRepository.releaseVlanKey(rangeId, key);
            this.vlanRepository.deleteItem(vlanId);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    public VlanKeyRequest allocateVlan(VlanKeyRequest request) throws Exception {
        logger.debug("Allocate vlan key, request: {}", request);

        Long key = vlanRangeRepository.allocateVlanKey(request.getRangeId());

        request.setKey(key);

        logger.info("Allocate vlan key success, request: {}", request);

        return request;
    }

    @Override
    public VlanKeyRequest releaseVlan(String networkType, String rangeId, Long key) throws Exception {
        logger.debug("Release vlan key, ipAddr: {}", key);

        vlanRangeRepository.releaseVlanKey(rangeId, key);

        VlanKeyRequest result = new VlanKeyRequest();
        result.setNetworkType(networkType);
        result.setRangeId(rangeId);
        result.setKey(key);

        logger.info("Release vlan key success, result: {}", result);

        return result;
    }

    @Override
    public VlanKeyRequest getVlan(String networkType, String rangeId, Long key) throws Exception {
        logger.debug("Get vlan key, rangeId: {}, ipAddr: {}", rangeId, key);

        VlanKeyAlloc vlanKeyAlloc = vlanRangeRepository.getVlanKeyAlloc(rangeId, key);
        if (vlanKeyAlloc.getNetworkType() != networkType) {
            throw new NetworkTypeInvalidException();
        }

        VlanKeyRequest result = new VlanKeyRequest();

        result.setNetworkType(vlanKeyAlloc.getNetworkType());
        result.setRangeId(vlanKeyAlloc.getRangeId());
        result.setKey(vlanKeyAlloc.getKey());

        logger.info("Get vlan key success, result: {}", result);

        return result;
    }

    @Override
    public NetworkVlanRangeRequest createRange(NetworkVlanRangeRequest request) throws Exception {
        logger.debug("Create vlan range, request: {}", request);

        vlanRangeRepository.createRange(request);

        logger.info("Create vlan range success, request: {}", request);

        return request;
    }

    @Override
    public NetworkVlanRangeRequest deleteRange(String rangeId) throws Exception {
        logger.debug("Delete vlan range, rangeId: {}", rangeId);

        NetworkVlanRange networkVlanRange = vlanRangeRepository.deleteRange(rangeId);

        NetworkVlanRangeRequest request = new NetworkVlanRangeRequest();
        request.setId(networkVlanRange.getId());
        request.setSegmentId(networkVlanRange.getSegmentId());
        request.setNetworkType(networkVlanRange.getNetworkType());
        request.setFirstKey(networkVlanRange.getFirstKey());
        request.setLastKey(networkVlanRange.getLastKey());
        request.setUsedKeys(networkVlanRange.getUsedKeys());
        request.setTotalKeys(networkVlanRange.getTotalKeys());

        logger.info("Delete vlan range success, request: {}", request);

        return request;
    }

    @Override
    public NetworkVlanRangeRequest getRange(String rangeId) throws Exception {
        logger.debug("Delete vlan range, rangeId: {}", rangeId);

        NetworkVlanRange networkVlanRange = vlanRangeRepository.getRange(rangeId);
        if (networkVlanRange == null) {
            throw new VlanRangeNotFoundException();
        }
        logger.info("Get vlan range success, ipAddressRange: {}", networkVlanRange);

        NetworkVlanRangeRequest request = new NetworkVlanRangeRequest();
        request.setId(networkVlanRange.getId());
        request.setSegmentId(networkVlanRange.getSegmentId());
        request.setNetworkType(networkVlanRange.getNetworkType());
        request.setFirstKey(networkVlanRange.getFirstKey());
        request.setLastKey(networkVlanRange.getLastKey());
        request.setUsedKeys(networkVlanRange.getUsedKeys());
        request.setTotalKeys(networkVlanRange.getTotalKeys());

        return request;
    }

    @Override
    public List<NetworkVlanRangeRequest> listRanges() {
        logger.debug("List vlan range");

        Map<String, NetworkVlanRange> ipAddrRangeMap = vlanRangeRepository.findAllItems();

        List<NetworkVlanRangeRequest> result = new ArrayList<>();
        ipAddrRangeMap.forEach((k,v) -> {
            NetworkVlanRangeRequest range = new NetworkVlanRangeRequest();
            range.setId(v.getId());
            range.setSegmentId(v.getSegmentId());
            range.setNetworkType(v.getNetworkType());
            range.setFirstKey(v.getFirstKey());
            range.setLastKey(v.getLastKey());
            range.setUsedKeys(v.getUsedKeys());
            range.setTotalKeys(v.getTotalKeys());
            result.add(range);
        });

        logger.info("List vlan range success, result: {}", result);

        return result;
    }
}
