/*
 *
 * Copyright 2019 The Alcor Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * /
 */

package com.futurewei.alcor.macmanager.generate.impl;

import com.futurewei.alcor.macmanager.generate.Generator;
import com.futurewei.alcor.web.entity.mac.MacRange;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static com.futurewei.alcor.macmanager.utils.MacUtils.*;

@Component
public class RandomGenerator implements Generator {

    private static final float BOTTOM_LOAD_FACTOR = 0.1f;
    private static final float LOW_LOAD_FACTOR = 0.25f;
    private static final float MIDDLE_LOAD_FACTOR = 0.5f;
    private static final float HIGH_LOAD_FACTOR = 0.75f;
    private static final float TOP_LOAD_FACTOR = 0.9f;

    @Value("{bottom-request-numbers: #{2}}")
    private int bottomRequestNumbers;

    @Value("{low-request-numbers: #{6}}")
    private int lowRequestNumbers;

    @Value("{middle-request-numbers: #{10}}")
    private int middleRequestNumbers;

    @Value("{high-request-numbers: #{20}}")
    private int highRequestNumbers;

    @Value("{top-request-numbers: #{50}}")
    private int topRequestNumbers;

    @Override
    public Set<String> allocateMac(String oui, MacRange macRange, long used, Function<String[], Set<String>> checker) {
        String fromHexSuffix = getMacSuffix(oui, macRange.getFrom());
        String toHexSuffix = getMacSuffix(oui, macRange.getTo());
        long start = macToLong(fromHexSuffix);
        long end = macToLong(toHexSuffix);
        float loadRate = (float) used/macRange.getCapacity();

        String[] macs = null;
        Set<String> checkResult = null;
        do {
            long next = RandomUtils.nextLong(start, end);
            if (loadRate < BOTTOM_LOAD_FACTOR) {
                macs = new String[]{longToMac(oui, next)};
                break;
            }

            int requestNumbers = getRequestNumbers(loadRate);
            requestNumbers = requestNumbers > macRange.getCapacity() ? (int) macRange.getCapacity() : requestNumbers;
            long left = next - requestNumbers / 2;
            long right = next + requestNumbers / 2 + requestNumbers % 2;

            if (left < start) {
                right += start - left;
                left = start;
            }

            if (right > end) {
                left -= right - end;
                right = end + 1;
            }

            macs = new String[requestNumbers];
            for (long i = left; i < right; i++) {
                macs[(int) (i - left)] = longToMac(oui, next);
            }

            checkResult = checker.apply(macs);
        }while(checkResult == null || checkResult.size() == macs.length);

        Set<String> newAllocatedMacs = new HashSet<>();
        for(String mac: macs){
            if (!checkResult.contains(mac)){
                newAllocatedMacs.add(mac);
            }
        }
        return newAllocatedMacs;
    }

    private int getRequestNumbers(float loadRate){
        if (loadRate < LOW_LOAD_FACTOR){
            return lowRequestNumbers;
        }else if (loadRate < MIDDLE_LOAD_FACTOR){
            return middleRequestNumbers;
        }else if (loadRate < HIGH_LOAD_FACTOR){
            return highRequestNumbers;
        }
        return topRequestNumbers;
    }
}
