/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.vpcmanager.allocator;

import com.futurewei.alcor.vpcmanager.config.ConstantsConfig;
import com.futurewei.alcor.vpcmanager.exception.NetworkKeyInvalidException;
import com.futurewei.alcor.vpcmanager.exception.NetworkKeyNotEnoughException;
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
            return ConstantsConfig.keyNotEnoughReturnValue;
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
