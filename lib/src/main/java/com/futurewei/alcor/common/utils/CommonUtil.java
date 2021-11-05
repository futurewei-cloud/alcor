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

import com.google.common.collect.Lists;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;

import java.beans.FeatureDescriptor;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

public class CommonUtil {

    private static final List<Class<?>> baseClasses = Lists.newArrayList(Byte.class, Short.class, Integer.class,
            Long.class, Float.class, Double.class, Boolean.class, Character.class, String.class);

    public static byte[] fromIpAddressStringToByteArray(String ipAddressString) throws UnknownHostException {
        InetAddress ip = InetAddress.getByName(ipAddressString);
        byte[] bytes = ip.getAddress();

        return bytes;
    }

    public static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("Max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public static boolean isUUID(String string) {
        try {
            UUID.fromString(string);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Return true if class is byte short int long float double boolean char or String or base class type of packaging
     * @param clasz
     * @return Return true if class is byte short int long float double boolean
     * char or String or base class type of packaging
     */
    public static boolean isBaseClass(Class<?> clasz){
        if(clasz.isPrimitive()){
            return true;
        }
        return baseClasses.contains(clasz);
    }

    /**
     * Return a bean null property names
     * @param bean a bean entity
     * @return return a bean null property names
     */
    public static String[] getBeanNullPropertyNames(Object bean){
        final BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
        return Stream.of(beanWrapper.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName -> {
                    try {
                        return beanWrapper.getPropertyValue(propertyName) == null;
                    } catch (BeansException e) {
                        return true;
                    }
                }).toArray(String[]::new);
    }

    /**
     * Determine if a given string is empty or null
     * @param an input string
     * @return a boolean value, true if null or empty
     */
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }

    /**
     *  Return CacheConfiguration for transaction
     * @param cacheName input String
     * @return  return cache configuration
     */

    public static CacheConfiguration getCacheConfiguration(String cacheName) {
        CacheConfiguration cfg = new CacheConfiguration();
        cfg.setName(cacheName);
        cfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        return cfg;
    }

    /**
     * Return a simple name of the class or member given a canonical name as string.
     * @param canon
     * @return simpleName
     */

    public static String getSimpleFromCanonicalName(String canon) {
        int lastDot = canon.lastIndexOf(".");
            if (lastDot != -1)
                return canon.substring(lastDot + 1);
            else
                return canon;
    }

    /**
     * Replace all dots with underscore to make a schema name
     * @param className
     * @return
     */
    public static String getSchemaNameForCacheClass(String className) {
        return className.replaceAll("\\.", "_");
    }
}
