/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.privateipmanager;



import org.junit.Test;

import java.util.BitSet;

public class BitSetTest {

    @Test
    public void basicTest() {
        BitSet bitSet = new BitSet();

        int i = 0;
        int clearBit = 0;

        bitSet.set(1);
        bitSet.set(2);
        bitSet.set(3);
        bitSet.set(4);
        System.out.println(bitSet.size());
        System.out.println(bitSet.length());
        while (i < 1000000000) {
            clearBit = bitSet.nextClearBit(clearBit + 1);
            System.out.println(clearBit);
            i++;
        }

    }
}
