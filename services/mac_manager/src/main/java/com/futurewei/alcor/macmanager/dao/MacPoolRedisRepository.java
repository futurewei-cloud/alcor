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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
public class MacPoolRedisRepository implements ICacheRepository<String> {

    private static final String KEY = "mac_pool";

    private RedisTemplate<String, String> redisTemplate;

    private SetOperations setOperations;

    @Autowired
    public MacPoolRedisRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init() {
        setOperations = redisTemplate.opsForSet();
    }

    @Override
    public String findItem(String value) {
        if (setOperations.isMember(KEY, value))
            return value;
        else
            return null;
    }

    @Override
    public Map findAllItems() {
        return (Map) setOperations.members(KEY);
    }

    @Override
    public void addItem(String newItem) {
        Logger logger = LoggerFactory.getLogger();
        logger.log(Level.INFO, newItem);
        if (setOperations.isMember(KEY, newItem) == false)
            setOperations.add(KEY, newItem);
    }

    @Override
    public void deleteItem(String value) {
        setOperations.remove(KEY, value);
    }

    public String getItem() {
        return (String) setOperations.randomMember(KEY);
    }

    public long getSize() {
        return setOperations.size(KEY);
    }
}
