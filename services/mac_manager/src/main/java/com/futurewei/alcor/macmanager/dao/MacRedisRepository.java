/*Copyright 2019 The Alcor Authors.

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

package com.futurewei.alcor.macmanager.dao;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.repo.ICacheRepository;
import com.futurewei.alcor.macmanager.entity.MacState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
public class MacRedisRepository implements ICacheRepository<MacState> {

    private String KEY = "mac_state";

    private RedisTemplate<String, MacState> redisTemplate;

    private HashOperations hashOperations;

    @Autowired
    public MacRedisRepository(RedisTemplate<String, MacState> redisTemplate) {

        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public MacState findItem(String id) {

        return (MacState) hashOperations.get(KEY, id);
    }

    @Override
    public Map findAllItems() {
        return hashOperations.entries(KEY);
    }

    @Override
    public void addItem(MacState newItem) {
        Logger logger = LoggerFactory.getLogger();
        logger.log(Level.INFO, "mac address:" + newItem.getMacAddress());
        hashOperations.putIfAbsent(KEY, newItem.getMacAddress(), newItem);
    }

    @Override
    public void deleteItem(String id) {
        hashOperations.delete(KEY, id);
    }

    public void updateItem(MacState newItem) {
        hashOperations.put(KEY, newItem.getMacAddress(), newItem);
    }

    public MacState findMac(String id) {
        return (MacState) hashOperations.get(KEY, id);
    }

    public void setKey(String key) {
        KEY = key;
    }

    public boolean exisingOui(String oui) {

        return redisTemplate.hasKey(oui);
    }
}

