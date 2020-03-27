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

package com.futurewei.vpcmanager.dao;

import com.futurewei.common.logging.Logger;
import com.futurewei.common.logging.LoggerFactory;
import com.futurewei.common.repo.ICacheRepository;
import com.futurewei.vpcmanager.entity.VpcState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
public class VpcRedisRepository implements ICacheRepository<VpcState> {

    private static final String KEY = "VpcState";

    private RedisTemplate<String, VpcState> redisTemplate;

    private HashOperations hashOperations;

    @Autowired
    public VpcRedisRepository(RedisTemplate<String, VpcState> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public VpcState findItem(String id) {
        return (VpcState) hashOperations.get(KEY, id);
    }

    @Override
    public Map findAllItems() {
        return hashOperations.entries(KEY);
    }

    @Override
    public void addItem(VpcState newItem) {
        Logger logger = LoggerFactory.getLogger();
        logger.log(Level.INFO, "Vpc Id:" + newItem.getId());
        hashOperations.put(KEY, newItem.getId(), newItem);
    }

    @Override
    public void deleteItem(String id) {
        hashOperations.delete(KEY, id);
    }
}
