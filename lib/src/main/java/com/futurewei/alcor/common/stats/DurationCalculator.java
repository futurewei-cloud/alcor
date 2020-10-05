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
package com.futurewei.alcor.common.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DurationCalculator {
    private static final Logger LOG = LoggerFactory.getLogger(DurationCalculator.class);

    private static Map<Long, Long> startTimes = new HashMap<>();

    public static void begin() {
        long id = Thread.currentThread().getId();
        startTimes.put(id, System.nanoTime());
    }

    public static void end(String description) {
        long id = Thread.currentThread().getId();
        long endTime = System.nanoTime();
        long startTime;
        if (startTimes.containsKey(id)) {
            startTime = startTimes.get(id);
        } else {
            startTime = endTime;
        }

        long duration = endTime - startTime;

        LOG.info("{} startTime: {}ns, endTime: {}ns, duration: {}ms",
                description, startTime, endTime, duration/1000000);
    }
}
