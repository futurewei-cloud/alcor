package com.futurewei.alcor.web.allocator;

import com.futurewei.alcor.common.exception.NetworkKeyInvalidException;
import com.futurewei.alcor.common.exception.NetworkKeyNotEnoughException;
import lombok.Data;

import java.util.BitSet;

@Data
public class NetworkKeyAllocator implements KeyAllocator {

    private BitSet bitSet;
    private int firstKey;
    private int lastKey;
    private int keyNum;
    private Long key;

    public NetworkKeyAllocator(int firstKey, int lastKey) {
        this.firstKey = firstKey;
        this.lastKey = lastKey;
        keyNum = lastKey - firstKey + 1;
        bitSet = new BitSet();
    }

    @Override
    public Long allocate() throws Exception {
        int freeBit = bitSet.nextClearBit(0);
        if (freeBit < 0 || freeBit >= keyNum) {
            throw new NetworkKeyNotEnoughException();
        }
        bitSet.set(freeBit);
        int index = firstKey + freeBit;
        key = Long.parseLong(String.valueOf(index));
        return key;
    }

    @Override
    public void release(Long key) throws Exception {
        int bit = Integer.parseInt(String.valueOf((long)key));
        if (bit < firstKey || bit > lastKey  ) {
            throw new NetworkKeyInvalidException();
        }
        int index = bit - firstKey;
        bitSet.clear(index);
    }

    @Override
    public boolean validate(Long key) {
        int bit = Integer.parseInt(String.valueOf((long)key));
        return bit >= firstKey && bit <= lastKey;
    }

}
