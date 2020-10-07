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

import com.google.common.collect.Lists;
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
}
