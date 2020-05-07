package com.futurewei.alcor.web.allocator;

import com.futurewei.alcor.common.exception.KeyInvalidException;
import com.futurewei.alcor.common.exception.KeyNotEnoughException;

import java.util.BitSet;

public class VlanKeyAllocator implements KeyAllocator {

    private BitSet bitSet;
    private int firstKey;
    private int lastKey;
    private int keyNum;
    private Long key;

    public VlanKeyAllocator (int firstKey, int lastKey) {
        this.firstKey = firstKey;
        this.lastKey = lastKey;
        keyNum = lastKey - firstKey + 1;
        bitSet = new BitSet();
    }

    @Override
    public Long allocate() throws Exception {
        int freeBit = bitSet.nextClearBit(0);
        if (freeBit < 0 || freeBit >= keyNum) {
            throw new KeyNotEnoughException();
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
            throw new KeyInvalidException();
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
