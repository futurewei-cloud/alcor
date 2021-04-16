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
package com.futurewei.alcor.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    private static final ThreadLocal<SimpleDateFormat> keystoneFormatLocal = new ThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(KEYSTONE_TOKEN_DATE_PATTERN);
        }
    };

    public static final String KEYSTONE_TOKEN_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static SimpleDateFormat getKeystoneDateFormat() {
        return keystoneFormatLocal.get();
    }

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
