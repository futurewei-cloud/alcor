package com.futurewei.alcor.web.allocator;

import com.futurewei.alcor.common.exception.KeyInvalidException;
import com.futurewei.alcor.common.exception.KeyNotEnoughException;

import java.util.BitSet;

public class VxlanKeyAllocator implements KeyAllocator {

    private BitSet bitSet;
    private int partition; // 0 - 1000
    private Long key;

    public VxlanKeyAllocator (int partition) {
        bitSet = new BitSet();
        this.partition = partition;
    }

    @Override
    public Long allocate() throws Exception {
        int freeBit = bitSet.nextClearBit(partition * 16000);
        if (freeBit < (partition * 16000) || freeBit >= ((partition + 1) * 16000)) {
            throw new KeyNotEnoughException();
        }
        bitSet.set(freeBit);
        key = Long.parseLong(String.valueOf(freeBit));
        return key;
    }

    @Override
    public void release(Long key) throws Exception {
        int bit = Integer.parseInt(String.valueOf((long)key));
        if (!validate(key)) {
            throw new KeyInvalidException();
        }
        bitSet.clear(bit);
    }

    @Override
    public boolean validate(Long key) {
        int bit = Integer.parseInt(String.valueOf((long)key));
        return bit >= 0 && bit < ((partition + 1) * 16000);
    }
}
