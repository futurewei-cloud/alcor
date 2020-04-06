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
import com.futurewei.alcor.macmanager.entity.OuiState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
public class OuiRedisRepository implements ICacheRepository<OuiState> {

    private static final String KEY = "OuiState";

    private RedisTemplate<String, String> redisTemplate;

    private HashOperations hashOperations;

    @Autowired
    public OuiRedisRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public OuiState findItem(String id) {

        return (OuiState) hashOperations.get(KEY, id);
    }

    @Override
    public Map findAllItems() {
        return hashOperations.entries(KEY);
    }

    @Override
    public void addItem(OuiState newItem) {
        Logger logger = LoggerFactory.getLogger();
        logger.log(Level.INFO, "oui:" + newItem.getOu());
        hashOperations.put(KEY, newItem.getOu(), newItem.getOui());
    }

    @Override
    public void deleteItem(String id) {
        hashOperations.delete(KEY, id);
    }

    public String findOui(String ou) {
        return (String) hashOperations.get(KEY, ou);
    }
}
