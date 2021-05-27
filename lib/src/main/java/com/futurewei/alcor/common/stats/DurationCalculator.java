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
