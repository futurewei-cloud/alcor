package com.futurewei.alcor.vpcmanager.service.Impl;

import com.futurewei.alcor.common.constants.NetworkType;
import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.exception.NetworkTypeInvalidException;
import com.futurewei.alcor.common.exception.VlanRangeNotFoundException;
import com.futurewei.alcor.vpcmanager.dao.*;
import com.futurewei.alcor.vpcmanager.service.SegmentService;
import com.futurewei.alcor.web.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SegmentServiceImpl implements SegmentService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private VlanRangeRepository vlanRangeRepository;

    @Autowired
    private VxlanRangeRepository vxlanRangeRepository;

    @Autowired
    private VlanRepository vlanRepository;

    @Autowired
    private VxlanRepository vxlanRepository;

    @Autowired
    private GreRepository greRepository;

    @Override
    public Long addVlanEntity(String segmentId, String vlanId, String networkType) throws Exception {

        Long key = null;
        String rangeId = null;

        try {
            NetworkVlanType vlan = new NetworkVlanType();
            // check if range exist in db
            Map<String, NetworkVlanRange> map = this.vlanRangeRepository.findAllItems();
            if (map.size() == 0) {
                rangeId = UUID.randomUUID().toString();
                NetworkRangeRequest request = new NetworkRangeRequest(rangeId, segmentId, networkType, NetworkType.VLAN_PARTITION, NetworkType.VLAN_FIRST_KEY, NetworkType.VLAN_LAST_KEY);
                this.vlanRangeRepository.createRange(request);
                map = this.vlanRangeRepository.findAllItems();
            }
            for (Map.Entry<String, NetworkVlanRange> entry : map.entrySet()) {
                rangeId = entry.getKey();
            }

            key = this.vlanRangeRepository.allocateVlanKey(rangeId);

            //vlan.setMtu(mtu);
            vlan.setSegmentId(segmentId);
            vlan.setVlanId(vlanId);
            vlan.setKey(key);

            this.vlanRepository.addItem(vlan);
            logger.info("Allocate vlan key success, key: " + key);
            return key;

        } catch (Exception e) {
            this.vlanRepository.deleteItem(vlanId);
            this.vlanRangeRepository.releaseVlanKey(rangeId,key);
            logger.info("Allocate vlan key or db operation failed");
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    public Long addVxlanEntity(String segmentId, String vlanId, String networkType) throws Exception {

        Long key = null;
        String rangeId = null;
        Random ran = new Random();
        Map<Integer, String> partitionsAndRangeIds = new HashMap<>();

        try {
            NetworkVxlanType vxlan = new NetworkVxlanType();

            // Find all partitions exist in db
            Map<String, NetworkVxlanRange> map = this.vxlanRangeRepository.findAllItems();
            for (Map.Entry<String, NetworkVxlanRange> entry : map.entrySet()) {
                int temp_partition = entry.getValue().getPartition();
                String temp_rangeId = entry.getValue().getId();
                partitionsAndRangeIds.put(temp_partition, temp_rangeId);
            }

            // Randomly allocate a partition and check if the partition exist in db
            int partition = ran.nextInt(NetworkType.VXLAN_PARTITION);
            if (!partitionsAndRangeIds.containsKey(partition)) {
                rangeId = UUID.randomUUID().toString();
                int firstKey = partition * NetworkType.VXLAN_ONE_PARTITION_SIZE;
                int lastKey = (partition + 1) * NetworkType.VXLAN_ONE_PARTITION_SIZE;
                NetworkRangeRequest request = new NetworkRangeRequest(rangeId, segmentId, networkType, partition, firstKey, lastKey);
                this.vxlanRangeRepository.createRange(request);
            }else {
                rangeId = partitionsAndRangeIds.get(partition);
            }

            key = this.vxlanRangeRepository.allocateVxlanKey(rangeId);
            vxlan.setSegmentId(segmentId);
            vxlan.setVxlanId(vlanId);
            vxlan.setKey(key);

            this.vxlanRepository.addItem(vxlan);
            logger.info("Allocate vxlan key success, key: " + key);
            return key;

        } catch (Exception e) {
            this.vxlanRepository.deleteItem(vlanId);
            this.vxlanRangeRepository.releaseVxlanKey(rangeId,key);
            logger.info("Allocate vlan key or db operation failed");
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    public Long addGreEntity(String segmentId, String vlanId, String networkType) throws DatabasePersistenceException {
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
    public void releaseVxlanEntity(String vxlanId, Long key) throws DatabasePersistenceException {
        try {
            Map<String, NetworkVxlanRange> map = this.vxlanRangeRepository.findAllItems();
            if (map.size() == 0) {
                // TO DO: throw exception
                return;
            }

            String rangeId = "";
            for (Map.Entry<String, NetworkVxlanRange> entry : map.entrySet()) {
                rangeId = entry.getKey();
                NetworkVxlanRange range = entry.getValue();
                if (range.getFirstKey() <= key && range.getLastKey() >= key) {
                    break;
                }
            }

            this.vxlanRangeRepository.releaseVxlanKey(rangeId, key);
            this.vxlanRepository.deleteItem(vxlanId);
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

        KeyAlloc keyAlloc = vlanRangeRepository.getVlanKeyAlloc(rangeId, key);
        if (keyAlloc.getNetworkType() != networkType) {
            throw new NetworkTypeInvalidException();
        }

        VlanKeyRequest result = new VlanKeyRequest();

        result.setNetworkType(keyAlloc.getNetworkType());
        result.setRangeId(keyAlloc.getRangeId());
        result.setKey(keyAlloc.getKey());

        logger.info("Get vlan key success, result: {}", result);

        return result;
    }

    @Override
    public NetworkRangeRequest createRange(NetworkRangeRequest request) throws Exception {
        logger.debug("Create vlan range, request: {}", request);

        vlanRangeRepository.createRange(request);

        logger.info("Create vlan range success, request: {}", request);

        return request;
    }

    @Override
    public NetworkRangeRequest deleteRange(String rangeId) throws Exception {
        logger.debug("Delete vlan range, rangeId: {}", rangeId);

        NetworkVlanRange networkVlanRange = vlanRangeRepository.deleteRange(rangeId);

        NetworkRangeRequest request = new NetworkRangeRequest();
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
    public NetworkRangeRequest getRange(String rangeId) throws Exception {
        logger.debug("Delete vlan range, rangeId: {}", rangeId);

        NetworkVlanRange networkVlanRange = vlanRangeRepository.getRange(rangeId);
        if (networkVlanRange == null) {
            throw new VlanRangeNotFoundException();
        }
        logger.info("Get vlan range success, ipAddressRange: {}", networkVlanRange);

        NetworkRangeRequest request = new NetworkRangeRequest();
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
    public List<NetworkRangeRequest> listRanges() {
        logger.debug("List vlan range");

        Map<String, NetworkVlanRange> ipAddrRangeMap = vlanRangeRepository.findAllItems();

        List<NetworkRangeRequest> result = new ArrayList<>();
        ipAddrRangeMap.forEach((k,v) -> {
            NetworkRangeRequest range = new NetworkRangeRequest();
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
