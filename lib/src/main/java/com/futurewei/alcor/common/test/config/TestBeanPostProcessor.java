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

package com.futurewei.alcor.common.test.config;

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ignite.IgniteCacheFactory;
import com.futurewei.alcor.common.db.ignite.MockIgniteServer;
import org.apache.ignite.Ignite;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * this class config {@link CacheFactory#iCacheFactory} to a mock ignite client for micro service use
 *
 */
//@Component
public class TestBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName){
        if(bean instanceof CacheFactory){
            Field field = ReflectionUtils.findField(bean.getClass(), "iCacheFactory");
            assert field != null;
            ReflectionUtils.makeAccessible(field);
            Ignite ignite = MockIgniteServer.getIgnite();
            ReflectionUtils.setField(field, bean, new IgniteCacheFactory(ignite));
        }

        return bean;
    }

}
