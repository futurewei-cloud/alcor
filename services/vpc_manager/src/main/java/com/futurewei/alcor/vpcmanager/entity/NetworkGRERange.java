package com.futurewei.alcor.vpcmanager.entity;

import com.futurewei.alcor.vpcmanager.exception.NetworkKeyAllocNotFoundException;
import com.futurewei.alcor.vpcmanager.exception.NetworkKeyInvalidException;
import com.futurewei.alcor.vpcmanager.allocator.NetworkKeyAllocator;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class NetworkGRERange {

    private String id;
    private String networkType;
    private int partition;
    private int firstKey;
    private int lastKey;
    private int usedKeys;
    private int totalKeys;

    private NetworkKeyAllocator allocator;
    public Map allocated;

    public NetworkGRERange(String id, String networkType,int partition, int firstKey, int lastKey) {
        this.id = id;
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

        throw new NetworkKeyInvalidException();
    }
}
