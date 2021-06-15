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
package com.futurewei.alcor.vpcmanager.entity;

import com.futurewei.alcor.vpcmanager.config.ConstantsConfig;
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

        if (!key.equals(ConstantsConfig.keyNotEnoughReturnValue)) {
            KeyAlloc keyAlloc = new KeyAlloc(key, id, networkType);

            allocated.put(key, keyAlloc);
            updateUsedKeys();
        }

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

    public void tryToRelease(Long key) throws Exception {
        if (allocated.get(key) == null) {
            return;
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
