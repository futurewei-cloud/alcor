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
package com.futurewei.alcor.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static String localToUTC(String localTime, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date localDate= null;
        try {
            localDate = sdf.parse(localTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long localTimeInMillis = localDate.getTime();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(localTimeInMillis);
        int zoneOffset = calendar.get(java.util.Calendar.ZONE_OFFSET);
        int dstOffset = calendar.get(java.util.Calendar.DST_OFFSET);
        calendar.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        Date utcDate = new Date(calendar.getTimeInMillis());
        String utc = sdf.format(utcDate);

        return utc;
    }

}
