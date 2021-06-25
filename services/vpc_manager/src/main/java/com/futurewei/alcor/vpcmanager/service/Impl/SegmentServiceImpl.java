/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.vpcmanager.service.Impl;

import com.futurewei.alcor.common.constants.NetworkType;
import com.futurewei.alcor.common.enumClass.NetworkTypeEnum;
import com.futurewei.alcor.common.exception.DatabasePersistenceException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.vpcmanager.config.ConstantsConfig;
import com.futurewei.alcor.vpcmanager.dao.*;
import com.futurewei.alcor.vpcmanager.entity.*;
import com.futurewei.alcor.vpcmanager.exception.NetworkKeyNotEnoughException;
import com.futurewei.alcor.vpcmanager.exception.NetworkTypeInvalidException;
import com.futurewei.alcor.vpcmanager.exception.VlanRangeNotFoundException;
import com.futurewei.alcor.vpcmanager.service.SegmentService;
import com.futurewei.alcor.vpcmanager.service.VpcDatabaseService;
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
    private GreRangeRepository greRangeRepository;

    @Autowired
    private VlanRepository vlanRepository;

    @Autowired
    private VxlanRepository vxlanRepository;

    @Autowired
    private GreRepository greRepository;

    @Autowired
    private VpcDatabaseService vpcDatabaseService;

    /**
     * Create a vlan
     *
     * @param vlanId
     * @param networkType
     * @param vpcId
     * @return vlan key
     * @throws Exception
     */
    @Override
    @DurationStatistics
    public Long addVlanEntity(String vlanId, String networkType, String vpcId, Integer mtu) throws Exception {

        Long key = null;
        String rangeId = null;

        try {
            NetworkVlanType vlan = new NetworkVlanType();
            // check if range exist in db
            Map<String, NetworkVlanRange> map = this.vlanRangeRepository.findAllItems();
            if (map.size() == 0) {
                rangeId = UUID.randomUUID().toString();
                NetworkRangeRequest request = new NetworkRangeRequest(rangeId, networkType, NetworkType.VLAN_PARTITION, NetworkType.VLAN_FIRST_KEY, NetworkType.VLAN_LAST_KEY);
                rangeId = this.vlanRangeRepository.createRange(request);
                map = this.vlanRangeRepository.findAllItems();
            }
            for (Map.Entry<String, NetworkVlanRange> entry : map.entrySet()) {
                rangeId = entry.getKey();
            }

            key = this.vlanRangeRepository.allocateVlanKey(rangeId);
            if (key.equals(ConstantsConfig.keyNotEnoughReturnValue)) {
                throw new NetworkKeyNotEnoughException();
            }

            vlan.setMtu(mtu);
            vlan.setVlanId(vlanId);
            vlan.setKey(key);

            this.vlanRepository.addItem(vlan);
            logger.info("Allocate vlan key success, key: " + key);
            return key;

        } catch (DatabasePersistenceException e) {
            this.vlanRepository.deleteItem(vlanId);
            this.vlanRangeRepository.releaseVlanKey(rangeId, key);
            logger.info("Allocate vlan key or db operation failed");
            throw e;
        } catch (NetworkKeyNotEnoughException e) {
            throw e;
        }
    }

    /**
     * Create a vxlan
     *
     * @param vxlanId
     * @param networkType
     * @param vpcId
     * @return vxlan key
     * @throws Exception
     */
    @Override
    @DurationStatistics
    public Long addVxlanEntity(String vxlanId, String networkType, String vpcId, Integer mtu) throws Exception {

        Long key = null;
        String partitionStringFormat = null;
        Random ran = new Random();

        try {
            NetworkVxlanType vxlan = new NetworkVxlanType();
            // the number of randomly allocating partition in vxlan range
            int round = 1;

            while (round <= ConstantsConfig.MAX_ROUNDS) {
                // Randomly allocate a partition and check if the partition exist in db
                int partition = ran.nextInt(NetworkType.VXLAN_PARTITION);
                partitionStringFormat = String.valueOf(partition);
                NetworkVxlanRange networkVxlanRange = this.vxlanRangeRepository.findItem(partitionStringFormat);
                if (networkVxlanRange == null) {
                    int firstKey = partition * NetworkType.VXLAN_ONE_PARTITION_SIZE;
                    int lastKey = (partition + 1) * NetworkType.VXLAN_ONE_PARTITION_SIZE;
                    NetworkRangeRequest request = new NetworkRangeRequest(partitionStringFormat, networkType, partition, firstKey, lastKey);
                    partitionStringFormat = this.vxlanRangeRepository.createRange(request);
                } else {
                    partitionStringFormat = networkVxlanRange.getId();
                }

                key = this.vxlanRangeRepository.allocateVxlanKey(partitionStringFormat);

                if (!key.equals(ConstantsConfig.keyNotEnoughReturnValue)) {
                    break;
                }
                round ++;
            }

            if (key.equals(ConstantsConfig.keyNotEnoughReturnValue)) {
                throw new NetworkKeyNotEnoughException();
            }

            vxlan.setMtu(mtu);
            vxlan.setVxlanId(vxlanId);
            vxlan.setKey(key);

            this.vxlanRepository.addItem(vxlan);
            logger.info("Allocate vxlan key success, key: " + key);
            return key;

        } catch (Exception e) {
            this.vxlanRepository.deleteItem(vxlanId);
            this.vxlanRangeRepository.releaseVxlanKey(partitionStringFormat, key);
            logger.info("Allocate vxlan key or db operation failed, key: " + key);
            throw e;
        }
    }

    /**
     * Create a gre
     *
     * @param greId
     * @param networkType
     * @param vpcId
     * @return gre key
     * @throws Exception
     */
    @Override
    @DurationStatistics
    public Long addGreEntity(String greId, String networkType, String vpcId, Integer mtu) throws Exception {

        Long key = null;
        String partitionStringFormat = null;
        Random ran = new Random();

        try {
            NetworkGREType gre = new NetworkGREType();
            // the number of randomly allocating partition in gre range
            int round = 1;

            while (round <= ConstantsConfig.MAX_ROUNDS) {
                // Randomly allocate a partition and check if the partition exist in db
                int partition = ran.nextInt(NetworkType.GRE_PARTITION);
                partitionStringFormat = String.valueOf(partition);
                NetworkGRERange networkGRERange = this.greRangeRepository.findItem(partitionStringFormat);
                if (networkGRERange == null) {
                    int firstKey = partition * NetworkType.GRE_ONE_PARTITION_SIZE;
                    int lastKey = (partition + 1) * NetworkType.GRE_ONE_PARTITION_SIZE;
                    NetworkRangeRequest request = new NetworkRangeRequest(partitionStringFormat, networkType, partition, firstKey, lastKey);
                    partitionStringFormat = this.greRangeRepository.createRange(request);
                } else {
                    partitionStringFormat = networkGRERange.getId();
                }

                key = this.greRangeRepository.allocateGreKey(partitionStringFormat);

                if (!key.equals(ConstantsConfig.keyNotEnoughReturnValue)) {
                    break;
                }
                round ++;
            }
            
            if (key.equals(ConstantsConfig.keyNotEnoughReturnValue)) {
                throw new NetworkKeyNotEnoughException();
            }

            gre.setMtu(mtu);
            gre.setGreId(greId);
            gre.setKey(key);

            this.greRepository.addItem(gre);
            logger.info("Allocate gre key success, key: " + key);
            return key;

        } catch (Exception e) {
            this.greRepository.deleteItem(greId);
            this.greRangeRepository.releaseGreKey(partitionStringFormat, key);
            logger.info("Allocate gre key or db operation failed");
            throw e;
        }
    }

    /**
     * Delete the vlan and release its associated resources
     *
     * @param vlanId
     * @param key
     * @throws DatabasePersistenceException
     */
    @Override
    @DurationStatistics
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

    /**
     * Delete the vxlan and release its associated resources
     *
     * @param vxlanId
     * @param key
     * @throws DatabasePersistenceException
     */
    @Override
    @DurationStatistics
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

    /**
     * Delete the gre and release its associated resources
     *
     * @param greId
     * @param key
     * @throws DatabasePersistenceException
     */
    @Override
    @DurationStatistics
    public void releaseGreEntity(String greId, Long key) throws DatabasePersistenceException {
        try {
            Map<String, NetworkGRERange> map = this.greRangeRepository.findAllItems();
            if (map.size() == 0) {
                // TO DO: throw exception
                return;
            }

            String rangeId = "";
            for (Map.Entry<String, NetworkGRERange> entry : map.entrySet()) {
                rangeId = entry.getKey();
                NetworkGRERange range = entry.getValue();
                if (range.getFirstKey() <= key && range.getLastKey() >= key) {
                    break;
                }
            }

            this.greRangeRepository.releaseGreKey(rangeId, key);
            this.greRepository.deleteItem(greId);
        } catch (Exception e) {
            throw new DatabasePersistenceException(e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public VlanKeyRequest allocateVlan(VlanKeyRequest request) throws Exception {
        logger.debug("Allocate vlan key, request: {}", request);

        Long key = vlanRangeRepository.allocateVlanKey(request.getRangeId());

        request.setKey(key);

        logger.info("Allocate vlan key success, request: {}", request);

        return request;
    }

    @Override
    @DurationStatistics
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
    @DurationStatistics
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
    @DurationStatistics
    public NetworkRangeRequest createRange(NetworkRangeRequest request) throws Exception {
        logger.debug("Create vlan range, request: {}", request);

        vlanRangeRepository.createRange(request);

        logger.info("Create vlan range success, request: {}", request);

        return request;
    }

    @Override
    @DurationStatistics
    public NetworkRangeRequest deleteRange(String rangeId) throws Exception {
        logger.debug("Delete vlan range, rangeId: {}", rangeId);

        NetworkVlanRange networkVlanRange = vlanRangeRepository.deleteRange(rangeId);

        NetworkRangeRequest request = new NetworkRangeRequest();
        request.setId(networkVlanRange.getId());
        request.setNetworkType(networkVlanRange.getNetworkType());
        request.setFirstKey(networkVlanRange.getFirstKey());
        request.setLastKey(networkVlanRange.getLastKey());
        request.setUsedKeys(networkVlanRange.getUsedKeys());
        request.setTotalKeys(networkVlanRange.getTotalKeys());

        logger.info("Delete vlan range success, request: {}", request);

        return request;
    }

    @Override
    @DurationStatistics
    public NetworkRangeRequest getRange(String rangeId) throws Exception {
        logger.debug("Delete vlan range, rangeId: {}", rangeId);

        NetworkVlanRange networkVlanRange = vlanRangeRepository.getRange(rangeId);
        if (networkVlanRange == null) {
            throw new VlanRangeNotFoundException();
        }
        logger.info("Get vlan range success, ipAddressRange: {}", networkVlanRange);

        NetworkRangeRequest request = new NetworkRangeRequest();
        request.setId(networkVlanRange.getId());
        request.setNetworkType(networkVlanRange.getNetworkType());
        request.setFirstKey(networkVlanRange.getFirstKey());
        request.setLastKey(networkVlanRange.getLastKey());
        request.setUsedKeys(networkVlanRange.getUsedKeys());
        request.setTotalKeys(networkVlanRange.getTotalKeys());

        return request;
    }

    @Override
    @DurationStatistics
    public List<NetworkRangeRequest> listRanges() {
        logger.debug("List vlan range");

        Map<String, NetworkVlanRange> ipAddrRangeMap = vlanRangeRepository.findAllItems();

        List<NetworkRangeRequest> result = new ArrayList<>();
        ipAddrRangeMap.forEach((k, v) -> {
            NetworkRangeRequest range = new NetworkRangeRequest();
            range.setId(v.getId());
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

    @Override
    public void createDefaultNetworkTypeTable() throws Exception {
        for (int i = 0; i < NetworkType.VXLAN_PARTITION; i++) {
            int firstKey = i * NetworkType.VXLAN_ONE_PARTITION_SIZE;
            int lastKey = (i + 1) * NetworkType.VXLAN_ONE_PARTITION_SIZE;
            String partitionStringFormat = String.valueOf(i);
            NetworkRangeRequest request = new NetworkRangeRequest(partitionStringFormat, NetworkTypeEnum.VXLAN.getNetworkType(), i, firstKey, lastKey);
            partitionStringFormat = this.vxlanRangeRepository.createRange(request);
        }

        for (int i = 0; i < NetworkType.GRE_PARTITION; i++) {
            int firstKey = i * NetworkType.GRE_ONE_PARTITION_SIZE;
            int lastKey = (i + 1) * NetworkType.GRE_ONE_PARTITION_SIZE;
            String partitionStringFormat = String.valueOf(i);
            NetworkRangeRequest request = new NetworkRangeRequest(partitionStringFormat, NetworkTypeEnum.GRE.getNetworkType(), i, firstKey, lastKey);
            partitionStringFormat = this.greRangeRepository.createRange(request);
        }
    }
}
