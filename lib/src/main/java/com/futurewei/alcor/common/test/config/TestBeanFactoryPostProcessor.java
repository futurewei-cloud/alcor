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

import com.futurewei.alcor.common.db.ignite.IgniteCacheFactory;
import com.futurewei.alcor.common.db.ignite.MockIgniteServer;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.logging.Level;

/**
 * in unit test environment it try to init a real Ignite server, but lot of cases don't need
 * a real Ignite server, so make Ignite BeanDefinition to null
 */
@Component
public class TestBeanFactoryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger();
    private static final String IGNITE_BEAN_FACTORY_NAME = "igniteClientFactoryInstance";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        try {
            System.out.println(Arrays.asList(registry.getBeanDefinitionNames()).toString());
            registry.removeBeanDefinition(IGNITE_BEAN_FACTORY_NAME);
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(IgniteCacheFactory.class, () -> {
                        return new IgniteCacheFactory(MockIgniteServer.getIgnite());
                    });
            registry.registerBeanDefinition(IGNITE_BEAN_FACTORY_NAME, beanDefinitionBuilder.getRawBeanDefinition());
        } catch (NoSuchBeanDefinitionException e) {
            LOG.log(Level.WARNING, "get ignite client bean failed : " + e.getMessage());
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
