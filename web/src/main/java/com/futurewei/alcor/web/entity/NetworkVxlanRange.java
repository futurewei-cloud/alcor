package com.futurewei.alcor.web.entity;

import com.futurewei.alcor.common.exception.KeyInvalidException;
import com.futurewei.alcor.common.exception.NetworkKeyAllocNotFoundException;
import com.futurewei.alcor.web.allocator.NetworkKeyAllocator;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class NetworkVxlanRange {

    private String id;
    private String segmentId;
    private String networkType;
    private int partition;
    private int firstKey;
    private int lastKey;
    private int usedKeys;
    private int totalKeys;

    private NetworkKeyAllocator allocator;
    public Map allocated;

    public NetworkVxlanRange(String id, String segmentId, String networkType,int partition, int firstKey, int lastKey) {
        this.id = id;
        this.segmentId = segmentId;
        this.networkType = networkType;
        this.partition = partition;
        this.firstKey = firstKey;
        this.lastKey = lastKey;
        totalKeys = lastKey - firstKey + 1;
        allocator = new NetworkKeyAllocator(firstKey, lastKey);
        allocated = new HashMap<Long, KeyAlloc>();
    }

    private void updateUsedKeys() {
        usedKeys = allocated.size();
    }

    public Long allocateKey() throws Exception {
        Long key = allocator.allocate();
        KeyAlloc keyAlloc = new KeyAlloc(key, id, networkType);

        allocated.put(key, keyAlloc);
        updateUsedKeys();

        return key;
    }

    public void release(Long key) throws Exception {
        if (allocated.get(key) == null) {
            throw new NetworkKeyAllocNotFoundException();
        }

        allocator.release(key);
        allocated.remove(key);
        updateUsedKeys();
    }

    public KeyAlloc getNetworkKey(Long key) throws Exception {
        KeyAlloc keyAlloc = (KeyAlloc)allocated.get(key);
        if (keyAlloc != null) {
            return keyAlloc;
        }

        if (allocator.validate(key)) {
            return new KeyAlloc(key, id, networkType);
        }

        throw new KeyInvalidException();
    }

}
