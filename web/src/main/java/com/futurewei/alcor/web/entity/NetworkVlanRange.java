package com.futurewei.alcor.web.entity;

import com.futurewei.alcor.common.enumClass.NetworkTypeEnum;
import com.futurewei.alcor.common.exception.KeyInvalidException;
import com.futurewei.alcor.common.exception.VlanKeyAllocNotFoundException;
import com.futurewei.alcor.web.allocator.VlanKeyAllocator;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class NetworkVlanRange {

    private String id;
    private String segmentId;
    private String networkType;
    private int firstKey;
    private int lastKey;
    private int usedKeys;
    private int totalKeys;

    private VlanKeyAllocator allocator;
    public Map allocated;

    public NetworkVlanRange(String id, String segmentId, String networkType, int firstKey, int lastKey) {
        this.id = id;
        this.segmentId = segmentId;
        this.networkType = networkType;
        this.firstKey = firstKey;
        this.lastKey = lastKey;

        if (NetworkTypeEnum.VXLAN.getNetworkType().equals(networkType)) {

        } else if (NetworkTypeEnum.VLAN.getNetworkType().equals(networkType)) {
            totalKeys = lastKey - firstKey + 1;
            allocator = new VlanKeyAllocator(firstKey, lastKey);
            allocated = new HashMap<Long, VlanKeyAlloc>();
        }else if (NetworkTypeEnum.GRE.getNetworkType().equals(networkType)) {

        }
    }

    private void updateUsedKeys() {
        usedKeys = allocated.size();
    }

    public Long allocateVlan() throws Exception {
        Long key = allocator.allocate();
        VlanKeyAlloc vlanKeyAlloc = new VlanKeyAlloc(key, id, networkType);

        allocated.put(key, vlanKeyAlloc);
        updateUsedKeys();

        return key;
    }

    public void release(Long key) throws Exception {
        if (allocated.get(key) == null) {
            throw new VlanKeyAllocNotFoundException();
        }

        allocator.release(key);
        allocated.remove(key);
        updateUsedKeys();
    }

    public VlanKeyAlloc getVlanKey(Long key) throws Exception {
        VlanKeyAlloc vlanKeyAlloc = (VlanKeyAlloc)allocated.get(key);
        if (vlanKeyAlloc != null) {
            return vlanKeyAlloc;
        }

        if (allocator.validate(key)) {
            return new VlanKeyAlloc(key, id, networkType);
        }

        throw new KeyInvalidException();
    }
}
